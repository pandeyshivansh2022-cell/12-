const http = require('http');
const fs = require('fs');
const path = require('path');
const https = require('https');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

// Global safety guards to keep server resilient against disconnects & API timeouts
process.on('uncaughtException', (err) => {
  console.error('⚠️ Uncaught Exception intercepted:', err.message);
});
process.on('unhandledRejection', (reason) => {
  console.error('⚠️ Unhandled Rejection intercepted:', reason);
});

// Helper to load .env if not in process.env
function getApiKey() {
  if (process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY.trim()) {
    return process.env.GEMINI_API_KEY.trim();
  }
  const envPath = path.join(__dirname, '.env');
  if (fs.existsSync(envPath)) {
    const content = fs.readFileSync(envPath, 'utf8');
    for (const line of content.split('\n')) {
      const trimmed = line.trim();
      if (trimmed.startsWith('GEMINI_API_KEY=')) {
        return trimmed.replace('GEMINI_API_KEY=', '').replace(/["']/g, '').trim();
      }
    }
  }
  return '';
}

// Helper to call Gemini API with model fallback and multi-turn support
function callGemini(prompt, apiKey, systemInstruction, history = []) {
  return new Promise((resolve, reject) => {
    if (!apiKey) {
      resolve("Hello! I am Gangstream AI. Please ensure your GEMINI_API_KEY is available in the environment to enable full intelligence.");
      return;
    }

    const contents = [];
    if (Array.isArray(history) && history.length > 0) {
      for (const msg of history) {
        if (msg && msg.text) {
          contents.push({
            role: msg.role === 'user' ? 'user' : 'model',
            parts: [{ text: String(msg.text) }]
          });
        }
      }
    }
    contents.push({
      role: 'user',
      parts: [{ text: String(prompt) }]
    });

    const payloadObj = {
      contents: contents,
      systemInstruction: {
        parts: [{ text: systemInstruction || "You are Gangstream AI, a futuristic, highly intelligent, and empathetic cyber assistant designed for engaging multi-turn conversations, creative writing, and rapid technical solutions." }]
      }
    };

    const payload = JSON.stringify(payloadObj);

    function tryGenerate(modelName, isFallback = false) {
      const url = new URL(`https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`);
      const req = https.request(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(payload)
        }
      }, (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          try {
            const json = JSON.parse(data);
            if (json.candidates && json.candidates[0] && json.candidates[0].content && json.candidates[0].content.parts) {
              const text = json.candidates[0].content.parts.map(p => p.text || '').join('');
              resolve(text || "Response received from Gangstream AI.");
            } else if (json.error) {
              if (!isFallback) {
                tryGenerate('gemini-3.5-flash-lite', true);
              } else {
                resolve(`⚡ Gangstream Notice: ${json.error.message || 'API error'}`);
              }
            } else {
              resolve("Hello from Gangstream AI! Received your transmission.");
            }
          } catch (e) {
            resolve("Received your transmission. (Parsing response)");
          }
        });
      });

      req.on('error', (err) => {
        if (!isFallback) {
          tryGenerate('gemini-3.5-flash-lite', true);
        } else {
          reject(err);
        }
      });

      req.write(payload);
      req.end();
    }

    tryGenerate('gemini-3.5-flash', false);
  });
}

const server = http.createServer((req, res) => {
  // Enable CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  const pathname = parsedUrl.pathname;

  // Health check
  if (pathname === '/health' && req.method === 'GET') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'OK', service: 'Gangstream AI Web & API Server', timestamp: new Date() }));
    return;
  }

  // Chat API
  if (pathname === '/api/chat' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', async () => {
      try {
        const parsed = JSON.parse(body || '{}');
        const message = parsed.message || '';
        const persona = parsed.persona || 'general';
        const history = parsed.history || [];

        if (!message) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Message is required' }));
          return;
        }

        let systemInstruction = "You are Gangstream AI, an advanced cybernetic intelligence with lightning-fast reasoning, concise yet thorough explanations, and a sleek, futuristic persona.";
        if (persona === 'coder') {
          systemInstruction = "You are Gangstream AI Code Specialist. You specialize in clean, performant Kotlin, Jetpack Compose, HTML, JavaScript, Node.js, and modern tech architectures. Provide code snippets with clear explanations.";
        } else if (persona === 'creative') {
          systemInstruction = "You are Gangstream AI Cyber Poet & Creative Visionary. Craft imaginative, futuristic, vivid prose, metaphors, and evocative narratives.";
        } else if (persona === 'director') {
          systemInstruction = "You are Gangstream AI Creative Video & Media Director. Specialize in cinematic storyboards, pacing, camera angles, soundscapes, and visual aesthetics.";
        }

        const apiKey = getApiKey();
        const responseText = await callGemini(message, apiKey, systemInstruction, history);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ response: responseText }));
      } catch (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Failed to process message', details: err.message }));
      }
    });
    return;
  }

  // Image Generation & Vector Art Studio API
  if (pathname === '/api/generate-image' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', async () => {
      try {
        const parsed = JSON.parse(body || '{}');
        const prompt = parsed.prompt || '';
        const style = parsed.style || 'cyberpunk';
        const aspectRatio = parsed.aspectRatio || '1:1';

        if (!prompt) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Prompt is required' }));
          return;
        }

        const apiKey = getApiKey();
        const sysPrompt = `You are a graphic and generative design engine.
Given the user prompt: "${prompt}" and style: "${style}".
Generate a structured response with:
1. Enhanced Art Direction Prompt
2. Color Palette Hex Codes (array of 4-5 hex strings)
3. Scene Breakdown (lighting, foreground, background)
4. A complete, valid, standalone SVG illustration (enclosed in <svg>...</svg> with viewBox="0 0 600 400" using vibrant gradients, glow filters, paths, circles, and rects suited for modern web display in dark mode).

Format your response as a valid JSON object with keys:
"enhancedPrompt": string,
"palette": array of hex strings,
"sceneDescription": string,
"svgMarkup": string (the complete raw SVG tag with elements)
Do NOT include any markdown code fencing around the JSON output.`;

        const rawResult = await callGemini(`Generate visual art for: ${prompt} in ${style} style (aspect ratio: ${aspectRatio})`, apiKey, sysPrompt);
        
        let jsonRes;
        try {
          const cleaned = rawResult.replace(/```json/g, '').replace(/```/g, '').trim();
          jsonRes = JSON.parse(cleaned);
        } catch (e) {
          jsonRes = {
            enhancedPrompt: prompt,
            palette: ['#667eea', '#764ba2', '#00f2fe', '#4facfe', '#ff5722'],
            sceneDescription: rawResult,
            svgMarkup: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 400" width="100%" height="100%"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#667eea"/><stop offset="100%" stop-color="#764ba2"/></linearGradient></defs><rect width="600" height="400" fill="#0d1117"/><circle cx="300" cy="200" r="120" fill="url(#g)" opacity="0.8"/><text x="50%" y="50%" fill="#fff" font-family="sans-serif" font-size="22" font-weight="bold" text-anchor="middle" dominant-baseline="middle">${prompt}</text></svg>`
          };
        }

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(jsonRes));
      } catch (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Failed to generate visual art', details: err.message }));
      }
    });
    return;
  }

  // Video Storyboard & Script Studio API (Matching prompt 5 /api/generate-video)
  if (pathname === '/api/generate-video' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', async () => {
      try {
        const parsed = JSON.parse(body || '{}');
        const prompt = parsed.prompt || '';
        const duration = parsed.duration || 15;
        const style = parsed.style || 'cinematic';

        if (!prompt) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Prompt is required' }));
          return;
        }

        const apiKey = getApiKey();
        const sysPrompt = `You are a high-end AI Video Director and Storyboard Architect.
Given the concept: "${prompt}", target duration: ${duration} seconds, style: "${style}".
Generate a cinematic storyboard breakdown.
Format as JSON with keys:
"title": string,
"logline": string,
"totalDuration": number,
"soundtrackVibe": string,
"scenes": array of objects, where each scene has:
  "sceneNumber": number,
  "durationSeconds": number,
  "cameraShot": string (e.g. "Wide Crane In", "Macro Dolly Zoom"),
  "visualAction": string,
  "lightingMood": string,
  "audioCue": string,
  "previewColor": string (hex color for storyboard preview)
Do NOT include markdown fencing around the JSON.`;

        const rawResult = await callGemini(`Create video storyboard for: ${prompt} (${duration}s, ${style})`, apiKey, sysPrompt);

        let jsonRes;
        try {
          const cleaned = rawResult.replace(/```json/g, '').replace(/```/g, '').trim();
          jsonRes = JSON.parse(cleaned);
        } catch (e) {
          jsonRes = {
            title: `Gangstream: ${prompt}`,
            logline: prompt,
            totalDuration: duration,
            soundtrackVibe: "Futuristic ambient synthesizer and heavy synth bassline",
            scenes: [
              { sceneNumber: 1, durationSeconds: Math.round(duration / 3), cameraShot: "Drone Flyover", visualAction: `Opening panorama for ${prompt}`, lightingMood: "Neon violet & cyber blue", audioCue: "Rising crescendo", previewColor: "#667eea" },
              { sceneNumber: 2, durationSeconds: Math.round(duration / 3), cameraShot: "Medium Tracking", visualAction: `Dynamic focal movement revealing central subject`, lightingMood: "High-contrast rim lighting", audioCue: "Rhythmic bass pulse", previewColor: "#764ba2" },
              { sceneNumber: 3, durationSeconds: Math.round(duration / 3), cameraShot: "Hero Close-up", visualAction: `Climactic resolution and Gangstream AI emblem`, lightingMood: "Golden hour synth glow", audioCue: "Echoing reverb", previewColor: "#ff5722" }
            ]
          };
        }

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(jsonRes));
      } catch (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Failed to generate video script', details: err.message }));
      }
    });
    return;
  }

  // Static File Serving
  let filePath = path.join(PUBLIC_DIR, pathname === '/' ? 'index.html' : pathname);

  // Security check: ensure path is inside PUBLIC_DIR
  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    res.end('Forbidden');
    return;
  }

  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      // Fallback to index.html for SPA
      filePath = path.join(PUBLIC_DIR, 'index.html');
    }

    const ext = path.extname(filePath).toLowerCase();
    const mimeTypes = {
      '.html': 'text/html; charset=utf-8',
      '.css': 'text/css; charset=utf-8',
      '.js': 'application/javascript; charset=utf-8',
      '.json': 'application/json; charset=utf-8',
      '.png': 'image/png',
      '.jpg': 'image/jpeg',
      '.svg': 'image/svg+xml'
    };
    const contentType = mimeTypes[ext] || 'application/octet-stream';

    fs.readFile(filePath, (readErr, content) => {
      if (readErr) {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('Not Found');
        return;
      }
      res.writeHead(200, { 'Content-Type': contentType });
      res.end(content);
    });
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Gangstream AI Web Server running on port ${PORT}`);
});
