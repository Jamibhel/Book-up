package com.example.bookup.ai;

/**
 * Skeleton for AI Chat Service.
 * Paused as per user request. Shows FAQs instead of calling Cloud Functions.
 */
public class AICloudFunctionClient {
    public interface AIResponseCallback {
        void onSuccess(String response);
        void onError(String errorMessage, int errorCode);
    }

    public void sendMessage(String message, String subject, AIResponseCallback callback) {
        callback.onSuccess("AI Chat coming soon! We are working hard to bring you an expert tutor. Please do well to wait.\n\n" +
                "**Frequently Asked Questions:**\n\n" +
                "**Q: What is the AI Tutor?**\n" +
                "A: It is an advanced educational assistant designed to help you with specific subjects like Math, Science, and Coding.\n\n" +
                "**Q: When will it be available?**\n" +
                "A: We are currently in the final testing phase. It will be live in the next update!\n\n" +
                "**Q: How do I use the app in general?**\n" +
                "A: You can find tutors in the Search tab, book sessions via the Calendar on their profile, and chat in real-time.\n\n" +
                "**Q: How do I pay for materials?**\n" +
                "A: You can purchase premium materials directly in the app using your wallet or linked payment method.");
    }
}
