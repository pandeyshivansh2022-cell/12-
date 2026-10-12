const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { SessionsClient } = require('@google-cloud/dialogflow');
const { Configuration, OpenAIApi } = require('openai');
const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');
const util = require('util');
const execPromise = util.promisify(exec);

// Initialize Express
const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json({ limit: '10mb' }));

// Temp directory for media generation
const tempDir = path.join(__dirname, 'temp');
if (!fs.existsSync(tempDir)) fs.mkdirSync(tempDir);

// Dialogflow ES Setup
let sessionClient;
let sessionPath;
try {
  sessionClient = new SessionsClient({
    keyFilename: process.env.GOOGLE_APPLICATION_CREDENTIALS || 'path/to/your-service-account-key.json',
  });
  const projectId = process.env.GCP_PROJECT_ID || 'your-google-cloud-project-id';
  sessionPath = sessionClient.projectAgentSessionPath(projectId, 'gangstream-session-id');
} catch (e) {
  console.warn('Dialogflow credentials not loaded:', e.message);
}

// OpenAI Setup (for DALL·E 3)
const openaiConfig = new Configuration({
  apiKey: process.env.OPENAI_API_KEY,
});
const openai = new OpenAIApi(openaiConfig);

// Chat API Endpoint
app.post('/api/chat', async (req, res) => {
  try {
    const { message } = req.body;
    if (!message) {
      return res.status(400).json({ error: "Message is required" });
    }

    if (!sessionClient || !sessionPath) {
      return res.json({
        response: `[Gangstream Express Server] Received: "${message}". (Configure GOOGLE_APPLICATION_CREDENTIALS & GCP_PROJECT_ID for live Dialogflow ES agent)`
      });
    }

    const request = {
      session: sessionPath,
      queryInput: {
        text: {
          text: message,
          languageCode: 'en-US',
        },
      },
    };

    const responses = await sessionClient.detectIntent(request);
    const result = responses[0].queryResult;
    console.log('Dialogflow Response:', result.fulfillmentText);
    res.json({ response: result.fulfillmentText });
  } catch (error) {
    console.error('Error in /api/chat:', error);
    res.status(500).json({ error: "Failed to process chat request", details: error.message });
  }
});

// Generate Image Endpoint (DALL·E 3)
app.post('/api/generate-image', async (req, res) => {
  try {
    const { prompt } = req.body;
    if (!prompt) {
      return res.status(400).json({ error: "Prompt is required" });
    }

    const response = await openai.createImage({
      model: "dall-e-3",
      prompt: prompt,
      n: 1,
      size: "1024x1024",
    });

    const imageUrl = response.data.data[0].url;
    res.json({ imageUrl });
  } catch (error) {
    console.error('Error in /api/generate-image:', error);
    res.status(500).json({ error: "Failed to generate image", details: error.message });
  }
});

// Generate Video Endpoint (Text-to-Video or Image-to-Video)
app.post('/api/generate-video', async (req, res) => {
  try {
    const { prompt, imageUrl, duration = 5 } = req.body;
    if (!prompt && !imageUrl) {
      return res.status(400).json({ error: "Either prompt or imageUrl is required" });
    }

    let inputFile = imageUrl;
    if (!imageUrl) {
      const imageResponse = await openai.createImage({
        model: "dall-e-3",
        prompt: prompt,
        n: 1,
        size: "1024x1024",
      });
      inputFile = imageResponse.data.data[0].url;
    }

    const imagePath = path.join(tempDir, `input_${Date.now()}.jpg`);
    const downloadCmd = `curl -s -o "${imagePath}" "${inputFile}"`;
    await execPromise(downloadCmd);

    const outputPath = path.join(tempDir, `output_${Date.now()}.mp4`);
    const ffmpegCmd = `ffmpeg -y -loop 1 -i "${imagePath}" -c:v libx264 -t ${duration} -pix_fmt yuv420p "${outputPath}"`;
    await execPromise(ffmpegCmd);

    res.sendFile(outputPath, () => {
      try {
        if (fs.existsSync(imagePath)) fs.unlinkSync(imagePath);
        if (fs.existsSync(outputPath)) fs.unlinkSync(outputPath);
      } catch (err) {
        console.error('Error cleaning temp files:', err);
      }
    });
  } catch (error) {
    console.error('Error in /api/generate-video:', error);
    res.status(500).json({ error: "Failed to generate video", details: error.message });
  }
});

// Health Check Endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'OK', service: 'Gangstream AI Unified Backend', timestamp: new Date() });
});

// Start the server
app.listen(PORT, () => {
  console.log(`🚀 Gangstream AI Unified Backend running on http://localhost:${PORT}`);
});

module.exports = app;
