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
function callGemini(prompt, apiKey, systemInstruction, history = [], timeoutMs = 12000) {
  return new Promise((resolve) => {
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
    const candidateModels = ['gemini-3-flash-preview', 'gemini-3.1-flash-lite', 'gemini-3.8-flash', 'gemini-3.6-flash'];
    let modelIndex = 0;

    function tryNextModel() {
      if (modelIndex >= candidateModels.length) {
        resolve("Hello from Gangstream AI! All neural channels are currently active and ready for your prompt.");
        return;
      }

      const modelName = candidateModels[modelIndex++];
      const url = new URL(`https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`);
      
      let reqCompleted = false;
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
          if (reqCompleted) return;
          reqCompleted = true;
          try {
            const json = JSON.parse(data);
            if (json.candidates && json.candidates[0] && json.candidates[0].content && json.candidates[0].content.parts) {
              const text = json.candidates[0].content.parts.map(p => p.text || '').join('');
              resolve(text || "Response received from Gangstream AI.");
            } else if (json.error) {
              // Try next model if quota or 503 spike
              tryNextModel();
            } else {
              tryNextModel();
            }
          } catch (e) {
            tryNextModel();
          }
        });
      });

      req.setTimeout(timeoutMs, () => {
        if (!reqCompleted) {
          reqCompleted = true;
          req.destroy();
          tryNextModel();
        }
      });

      req.on('error', () => {
        if (!reqCompleted) {
          reqCompleted = true;
          tryNextModel();
        }
      });

      req.write(payload);
      req.end();
    }

    tryNextModel();
  });
}

// Wikipedia Knowledge Engine Integration
const wikiCache = new Map();

function httpsGetJson(urlStr, headers = {}, timeoutMs = 8000) {
  return new Promise((resolve, reject) => {
    try {
      const parsedUrl = new URL(urlStr);
      const req = https.get(parsedUrl, {
        headers: {
          'User-Agent': 'GangstreamAI-Bot/1.0 (https://gangstream.ai; encyclopedic-bot@gangstream.ai)',
          'Accept': 'application/json',
          ...headers
        }
      }, (res) => {
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          return httpsGetJson(res.headers.location, headers, timeoutMs).then(resolve).catch(reject);
        }
        if (res.statusCode !== 200) {
          return reject(new Error(`HTTP ${res.statusCode}`));
        }
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          try {
            resolve(JSON.parse(data));
          } catch (e) {
            reject(e);
          }
        });
      });
      req.setTimeout(timeoutMs, () => {
        req.destroy();
        reject(new Error('Wikipedia request timeout'));
      });
      req.on('error', reject);
    } catch (err) {
      reject(err);
    }
  });
}

async function getWikipediaSummary(title, lang = 'en') {
  if (!title) return null;
  const cacheKey = `summary:${lang}:${title.toLowerCase()}`;
  if (wikiCache.has(cacheKey)) {
    return wikiCache.get(cacheKey);
  }
  try {
    const encodedTitle = encodeURIComponent(title.replace(/\s+/g, '_'));
    const url = `https://${lang}.wikipedia.org/api/rest_v1/page/summary/${encodedTitle}`;
    const data = await httpsGetJson(url);
    if (data && data.title && data.extract) {
      const result = {
        title: data.title,
        description: data.description || '',
        extract: data.extract,
        thumbnail: data.thumbnail ? data.thumbnail.source : null,
        url: data.content_urls && data.content_urls.desktop ? data.content_urls.desktop.page : `https://${lang}.wikipedia.org/wiki/${encodedTitle}`,
        pageid: data.pageid || null,
        lang: lang
      };
      wikiCache.set(cacheKey, result);
      return result;
    }
  } catch (e) {
    // summary fetch failed
  }
  return null;
}

async function searchWikipedia(query, lang = 'en', limit = 6) {
  if (!query) return [];
  const cacheKey = `search:${lang}:${query.toLowerCase()}:${limit}`;
  if (wikiCache.has(cacheKey)) {
    return wikiCache.get(cacheKey);
  }
  try {
    const encoded = encodeURIComponent(query);
    const url = `https://${lang}.wikipedia.org/w/api.php?action=query&list=search&srsearch=${encoded}&srlimit=${limit}&utf8=&format=json`;
    const data = await httpsGetJson(url);
    if (data && data.query && Array.isArray(data.query.search)) {
      const items = data.query.search.map(item => ({
        title: item.title,
        snippet: (item.snippet || '').replace(/<[^>]+>/g, ''),
        pageid: item.pageid,
        url: `https://${lang}.wikipedia.org/wiki/${encodeURIComponent(item.title.replace(/\s+/g, '_'))}`
      }));
      wikiCache.set(cacheKey, items);
      return items;
    }
  } catch (e) {
    console.error('Wikipedia search error:', e.message);
  }
  return [];
}

async function getRandomWikipediaArticle(lang = 'en') {
  try {
    const url = `https://${lang}.wikipedia.org/api/rest_v1/page/random/summary`;
    const data = await httpsGetJson(url);
    if (data && data.title && data.extract) {
      return {
        title: data.title,
        description: data.description || '',
        extract: data.extract,
        thumbnail: data.thumbnail ? data.thumbnail.source : null,
        url: data.content_urls && data.content_urls.desktop ? data.content_urls.desktop.page : `https://${lang}.wikipedia.org/wiki/${encodeURIComponent(data.title.replace(/\s+/g, '_'))}`,
        pageid: data.pageid || null,
        lang: lang
      };
    }
  } catch (e) {
    console.error('Wikipedia random error:', e.message);
  }
  return null;
}

async function getWikipediaOnThisDay() {
  const now = new Date();
  const month = String(now.getUTCMonth() + 1).padStart(2, '0');
  const day = String(now.getUTCDate()).padStart(2, '0');
  const cacheKey = `onthisday:${month}:${day}`;
  if (wikiCache.has(cacheKey)) {
    return wikiCache.get(cacheKey);
  }
  try {
    const url = `https://en.wikipedia.org/api/rest_v1/feed/onthisday/events/${month}/${day}`;
    const data = await httpsGetJson(url);
    if (data && Array.isArray(data.events)) {
      const events = data.events.slice(0, 10).map(ev => ({
        year: ev.year,
        text: ev.text,
        pages: (ev.pages || []).slice(0, 2).map(p => ({
          title: p.title,
          extract: p.extract,
          url: p.content_urls && p.content_urls.desktop ? p.content_urls.desktop.page : `https://en.wikipedia.org/wiki/${encodeURIComponent(p.title)}`,
          thumbnail: p.thumbnail ? p.thumbnail.source : null
        }))
      }));
      wikiCache.set(cacheKey, events);
      return events;
    }
  } catch (e) {
    console.error('OnThisDay error:', e.message);
  }
  return [];
}

const server = http.createServer(async (req, res) => {
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

  // Wikipedia Search API
  if (pathname === '/api/wikipedia/search' && req.method === 'GET') {
    const q = parsedUrl.searchParams.get('q') || '';
    const lang = parsedUrl.searchParams.get('lang') || 'en';
    const limit = parseInt(parsedUrl.searchParams.get('limit') || '8');
    const results = await searchWikipedia(q, lang, limit);
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ query: q, results }));
    return;
  }

  // Wikipedia Page Summary API
  if (pathname === '/api/wikipedia/summary' && req.method === 'GET') {
    const title = parsedUrl.searchParams.get('title') || '';
    const lang = parsedUrl.searchParams.get('lang') || 'en';
    const summary = await getWikipediaSummary(title, lang);
    if (summary) {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(summary));
    } else {
      res.writeHead(404, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'Wikipedia article not found' }));
    }
    return;
  }

  // Wikipedia Random Article API
  if (pathname === '/api/wikipedia/random' && req.method === 'GET') {
    const lang = parsedUrl.searchParams.get('lang') || 'en';
    const article = await getRandomWikipediaArticle(lang);
    if (article) {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(article));
    } else {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'Failed to fetch random Wikipedia article' }));
    }
    return;
  }

  // Wikipedia On This Day API
  if (pathname === '/api/wikipedia/onthisday' && req.method === 'GET') {
    const events = await getWikipediaOnThisDay();
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ events }));
    return;
  }

  // Chat API with Wikipedia Encyclopedic Knowledge Grounding
  if (pathname === '/api/chat' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', async () => {
      try {
        const parsed = JSON.parse(body || '{}');
        const message = parsed.message || '';
        const persona = parsed.persona || 'general';
        const history = parsed.history || [];
        const useWikipedia = parsed.useWikipedia !== false;
        const lang = parsed.lang || 'en';

        if (!message) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Message is required' }));
          return;
        }

        let systemInstruction = "You are Gangstream AI, an advanced cybernetic intelligence with lightning-fast reasoning, concise yet thorough explanations, and a sleek, futuristic persona. You possess deep knowledge across science, technology, world history, arts, and encyclopedic facts.";
        if (persona === 'coder') {
          systemInstruction = "You are Gangstream AI Code Specialist. You specialize in clean, performant Kotlin, Jetpack Compose, HTML, JavaScript, Node.js, and modern tech architectures. Provide code snippets with clear explanations.";
        } else if (persona === 'creative') {
          systemInstruction = "You are Gangstream AI Cyber Poet & Creative Visionary. Craft imaginative, futuristic, vivid prose, metaphors, and evocative narratives.";
        } else if (persona === 'director') {
          systemInstruction = "You are Gangstream AI Creative Video & Media Director. Specialize in cinematic storyboards, pacing, camera angles, soundscapes, and visual aesthetics.";
        } else if (persona === 'scholar') {
          systemInstruction = "You are Gangstream AI Wikipedia Scholar & Universal Encyclopedist. You excel at synthesizing comprehensive, factual, well-structured encyclopedic knowledge with accurate historical dates, scientific rigor, and academic clarity.";
        }

        // Live Wikipedia Grounding Retrieval
        let wikiSource = null;
        if (useWikipedia) {
          try {
            // Clean up conversational boilerplate to extract primary entity or search query
            const cleanedQuery = message
              .replace(/^(who (is|was)|what (is|are|was)|where (is|are)|tell me about|explain|search wikipedia for|can you tell me about|how does|what do you know about|describe|history of)\s+/i, '')
              .replace(/[?!.,]+$/, '')
              .trim();

            const targetQuery = cleanedQuery.length > 1 ? cleanedQuery : message;
            const searchResults = await searchWikipedia(targetQuery, lang, 3);
            if (searchResults && searchResults.length > 0) {
              const topArticle = searchResults[0];
              wikiSource = await getWikipediaSummary(topArticle.title, lang);
            }
          } catch (wikiErr) {
            console.error('Wikipedia grounding error:', wikiErr.message);
          }
        }

        // Augment prompt with Wikipedia knowledge if available
        let augmentedMessage = message;
        if (wikiSource && wikiSource.extract) {
          augmentedMessage = `[WIKIPEDIA KNOWLEDGE CONTEXT]
Source Article: "${wikiSource.title}" (${wikiSource.url})
Wikipedia Summary: ${wikiSource.extract}
${wikiSource.description ? `Description: ${wikiSource.description}` : ''}

[USER QUESTION]
${message}

Please provide a comprehensive, highly insightful response utilizing the verified Wikipedia knowledge context above where appropriate. Keep the style sleek, engaging, and authoritative.`;
        }

        const apiKey = getApiKey();
        let responseText = await callGemini(augmentedMessage, apiKey, systemInstruction, history);
        
        if ((!responseText || responseText.startsWith("Hello from Gangstream AI! All neural channels")) && wikiSource && wikiSource.extract) {
          responseText = `### ${wikiSource.title}\n\n${wikiSource.extract}\n\n*Verified knowledge source: [Wikipedia Article](${wikiSource.url})*`;
        }

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          response: responseText,
          wikipedia: wikiSource ? {
            title: wikiSource.title,
            description: wikiSource.description,
            extract: wikiSource.extract,
            url: wikiSource.url,
            thumbnail: wikiSource.thumbnail
          } : null
        }));
      } catch (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Failed to process message', details: err.message }));
      }
    });
    return;
  }

  // Dynamic SVG Generator Helper
  function buildDynamicSvg(promptText, style = 'cyberpunk', palette = ['#00f2fe', '#7928ca', '#ff0080', '#1a1f35', '#0b0f19']) {
    const safeTitle = (promptText || 'Vector Art').slice(0, 32).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 400" width="100%" height="100%">
  <defs>
    <linearGradient id="bgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#070a14"/>
      <stop offset="50%" stop-color="#121829"/>
      <stop offset="100%" stop-color="#1a112c"/>
    </linearGradient>
    <linearGradient id="neonGrad1" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="${palette[0] || '#00f2fe'}"/>
      <stop offset="100%" stop-color="${palette[1] || '#7928ca'}"/>
    </linearGradient>
    <linearGradient id="sunGrad" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="${palette[2] || '#ff0080'}"/>
      <stop offset="100%" stop-color="${palette[0] || '#00f2fe'}"/>
    </linearGradient>
    <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur stdDeviation="6" result="blur"/>
      <feMerge>
        <feMergeNode in="blur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
  </defs>
  <rect width="600" height="400" fill="url(#bgGrad)"/>
  <circle cx="90" cy="60" r="1.5" fill="#fff" opacity="0.8"/>
  <circle cx="210" cy="80" r="1.2" fill="#00f2fe" opacity="0.7"/>
  <circle cx="390" cy="45" r="1.8" fill="#ff0080" opacity="0.8"/>
  <circle cx="510" cy="90" r="1.5" fill="#fff" opacity="0.7"/>
  <circle cx="300" cy="180" r="70" fill="url(#sunGrad)" filter="url(#glow)" opacity="0.85"/>
  <path d="M0,260 L60,260 L60,220 L100,220 L100,250 L150,250 L150,190 L190,190 L190,260 L240,260 L270,170 L300,170 L300,260 L360,260 L360,210 L410,210 L410,240 L460,240 L460,195 L500,195 L500,260 L600,260 L600,400 L0,400 Z" fill="#0c101c" opacity="0.95"/>
  <path d="M0,260 L600,260" stroke="url(#neonGrad1)" stroke-width="2" filter="url(#glow)"/>
  <g stroke="${palette[0] || '#00f2fe'}" stroke-width="1" opacity="0.35">
    <line x1="300" y1="260" x2="0" y2="400"/>
    <line x1="300" y1="260" x2="100" y2="400"/>
    <line x1="300" y1="260" x2="200" y2="400"/>
    <line x1="300" y1="260" x2="300" y2="400"/>
    <line x1="300" y1="260" x2="400" y2="400"/>
    <line x1="300" y1="260" x2="500" y2="400"/>
    <line x1="300" y1="260" x2="600" y2="400"/>
    <line x1="120" y1="280" x2="480" y2="280"/>
    <line x1="80" y1="310" x2="520" y2="310"/>
    <line x1="40" y1="350" x2="560" y2="350"/>
  </g>
  <rect x="150" y="325" width="300" height="42" rx="21" fill="rgba(10,15,28,0.85)" stroke="url(#neonGrad1)" stroke-width="1.5" filter="url(#glow)"/>
  <text x="300" y="351" fill="#fff" font-family="-apple-system, BlinkMacSystemFont, sans-serif" font-size="13" font-weight="700" text-anchor="middle" letter-spacing="1">${safeTitle}</text>
</svg>`;
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
          jsonRes = null;
        }

        if (!jsonRes || typeof jsonRes !== 'object') {
          const defaultPalette = style === 'synthwave' ? ['#ff007f', '#7928ca', '#00f2fe', '#ffe600'] :
                                 style === 'hologram' ? ['#00f2fe', '#4facfe', '#667eea', '#00ffff'] :
                                 style === 'anime' ? ['#ff4081', '#7c4dff', '#536dfe', '#00e5ff'] :
                                 ['#00f2fe', '#7928ca', '#ff0080', '#1a1f35'];
          jsonRes = {
            enhancedPrompt: `${prompt} rendered in rich ${style} aesthetics`,
            palette: defaultPalette,
            sceneDescription: `A striking ${style} vector composition depicting "${prompt}" with luminous neon gradients and retro-futuristic horizon perspective.`,
            svgMarkup: buildDynamicSvg(prompt, style, defaultPalette)
          };
        } else {
          if (!jsonRes.svgMarkup || !jsonRes.svgMarkup.includes('<svg')) {
            jsonRes.svgMarkup = buildDynamicSvg(prompt, style, jsonRes.palette || []);
          }
          if (!jsonRes.sceneDescription || jsonRes.sceneDescription.includes('All neural channels')) {
            jsonRes.sceneDescription = `Dynamic ${style} vector visualization for "${prompt}" featuring cybernetic illumination and balanced atmospheric depth.`;
          }
        }

        if (jsonRes.svgMarkup && !jsonRes.imageUrl) {
          jsonRes.imageUrl = 'data:image/svg+xml;utf8,' + encodeURIComponent(jsonRes.svgMarkup);
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

  // Video Storyboard & Script Studio API (Matching prompt 1 & 4 /api/generate-video)
  if (pathname === '/api/generate-video' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', async () => {
      try {
        const parsed = JSON.parse(body || '{}');
        const imageUrl = parsed.imageUrl || '';
        let prompt = parsed.prompt || '';
        if (!prompt && imageUrl) {
          prompt = 'Cinematic sequence based on input frame asset';
        }
        const duration = parseInt(parsed.duration || '15');
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
