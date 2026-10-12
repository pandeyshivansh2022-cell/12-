const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { Configuration, OpenAIApi } = require('openai');
const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');
const util = require('util');
const execPromise = util.promisify(exec);

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(bodyParser.json({ limit: '10mb' })); // Allow large image uploads

// OpenAI Setup (for image generation)
const openaiConfig = new Configuration({
  apiKey: process.env.OPENAI_API_KEY, // Set this in your environment variables
});
const openai = new OpenAIApi(openaiConfig);

// Ensure temp directory exists
const tempDir = path.join(__dirname, 'temp');
if (!fs.existsSync(tempDir)) fs.mkdirSync(tempDir);

// Generate Image Endpoint (DALL·E 3)
app.post('/api/generate-image', async (req, res) => {
  try {
    const { prompt } = req.body;
    if (!prompt) {
      return res.status(400).json({ error: "Prompt is required" });
    }

    // Generate image using DALL·E 3
    const response = await openai.createImage({
      model: "dall-e-3",
      prompt: prompt,
      n: 1,
      size: "1024x1024",
    });

    const imageUrl = response.data.data[0].url;
    res.json({ imageUrl });
  } catch (error) {
    console.error('Error generating image:', error);
    res.status(500).json({ error: "Failed to generate image", details: error.message });
  }
});

// Generate Video Endpoint (Text-to-Video or Image-to-Video)
app.post('/api/generate-video', async (req, res) => {
  try {
    const { prompt, imageUrl, duration = 5 } = req.body; // duration in seconds
    if (!prompt && !imageUrl) {
      return res.status(400).json({ error: "Either prompt or imageUrl is required" });
    }

    let inputFile = imageUrl;
    if (!imageUrl) {
      // Generate image first if no imageUrl is provided
      const imageResponse = await openai.createImage({
        model: "dall-e-3",
        prompt: prompt,
        n: 1,
        size: "1024x1024",
      });
      inputFile = imageResponse.data.data[0].url;
    }

    // Download the image (or use the provided URL)
    const imagePath = path.join(tempDir, `input_${Date.now()}.jpg`);
    const downloadCmd = `curl -s -o "${imagePath}" "${inputFile}"`;
    await execPromise(downloadCmd);

    // Generate video using FFmpeg (simple slideshow with text)
    const outputPath = path.join(tempDir, `output_${Date.now()}.mp4`);
    const ffmpegCmd = `ffmpeg -y -loop 1 -i "${imagePath}" -c:v libx264 -t ${duration} -pix_fmt yuv420p "${outputPath}"`;
    await execPromise(ffmpegCmd);

    // Send the video file
    res.sendFile(outputPath, () => {
      // Clean up temp files
      try {
        if (fs.existsSync(imagePath)) fs.unlinkSync(imagePath);
        if (fs.existsSync(outputPath)) fs.unlinkSync(outputPath);
      } catch (err) {
        console.error('Error cleaning temp files:', err);
      }
    });
  } catch (error) {
    console.error('Error generating video:', error);
    res.status(500).json({ error: "Failed to generate video", details: error.message });
  }
});

// Health Check Endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date() });
});

// Start the server
app.listen(PORT, () => {
  console.log(`🚀 Gangstream AI Media Generator running on http://localhost:${PORT}`);
});

module.exports = app;
