const functions = require('firebase-functions');
const admin = require('firebase-admin');
const rateLimit = require('express-rate-limit');
const { OpenAI } = require('openai');

// Initialize rate limiter
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 50 // limit each IP to 50 requests per windowMs
});

// Initialize OpenAI with environment variable
const openai = new OpenAI({
    apiKey: process.env.OPENAI_API_KEY
});

exports.processAIChatMessage = functions.https.onCall(async (data, context) => {
    // Validate authentication
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be logged in');
    }

    const { message, subject } = data;
    
    // Input validation
    if (!message || !subject) {
        throw new functions.https.HttpsError('invalid-argument', 'Message and subject are required');
    }

    try {
        // Get user's recent conversation history (last 5 messages)
        const history = await admin.firestore()
            .collection('ai_chat_messages')
            .where('userId', '==', context.auth.uid)
            .where('subject', '==', subject)
            .orderBy('timestamp', 'desc')
            .limit(5)
            .get();

        // Build conversation context
        const conversationHistory = history.docs
            .map(doc => doc.data())
            .reverse()
            .map(msg => `${msg.isAi ? 'AI' : 'User'}: ${msg.messageText}`)
            .join('\n');

        // Construct the prompt with subject context
        const prompt = `You are an expert tutor helping a student with ${subject}. 
Previous conversation:
${conversationHistory}

Student's question: ${message}

Provide a clear, educational response that helps the student understand the topic better. Include examples when relevant.`;

        // Call OpenAI API
        const response = await openai.chat.completions.create({
            model: "gpt-4",
            messages: [
                {
                    role: "system",
                    content: `You are an expert tutor specializing in ${subject}. 
                    Provide clear, educational responses that help students understand concepts deeply. 
                    Include examples and explanations that are appropriate for their level.
                    
                    Format your responses using markdown:
                    - Use code blocks with language specification for code examples (e.g., \`\`\`python)
                    - Use inline code with single backticks for short code references
                    - Use LaTeX notation for mathematical formulas ($...$ for inline, $$...$$  for blocks)
                    - Use **bold** and *italic* for emphasis
                    - Use bullet points and numbered lists for structured explanations
                    - Include proper headings (##) for different sections
                    
                    Example formatting:
                    \`\`\`python
                    def example():
                        print("Hello, World!")
                    \`\`\`
                    
                    Math example: $E = mc^2$ or $$\\sum_{i=1}^n i = \\frac{n(n+1)}{2}$$`
                },
                {
                    role: "user",
                    content: prompt
                }
            ],
            temperature: 0.7,
            max_tokens: 500
        });

        // Extract and format the AI response
        const aiResponse = response.choices[0].message.content.trim();
        
        return {
            response: aiResponse,
            timestamp: admin.firestore.FieldValue.serverTimestamp()
        };
    } catch (error) {
        console.error('AI Chat Error:', error);
        throw new functions.https.HttpsError('internal', 'Error processing your request');
    }
});