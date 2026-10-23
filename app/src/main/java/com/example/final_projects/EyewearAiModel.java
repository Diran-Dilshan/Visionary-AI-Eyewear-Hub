package com.example.final_projects;

import com.example.final_projects.network.FrameRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EyewearAiModel {

    private List<FrameRequest> catalogFrames = new ArrayList<>();

    public EyewearAiModel(List<FrameRequest> frames) {
        if (frames != null && !frames.isEmpty()) {
            this.catalogFrames = frames;
        } else {
            // Default knowledge base frames if database is empty
            catalogFrames.add(new FrameRequest("Classic Square Elite", "Modern thick-rimmed acetate frame with premium finish.", 129.0, "In Stock", 4.8, 156, "square", 10));
            catalogFrames.add(new FrameRequest("Retro Tortoiseshell", "Vintage tortoise patterns with warm tones and keyhole bridge.", 149.0, "In Stock", 4.6, 98, "glass", 8));
            catalogFrames.add(new FrameRequest("Aviator Legend", "Timeless metal frame design for a bold and sleek look.", 189.0, "In Stock", 4.9, 210, "aviator", 5));
            catalogFrames.add(new FrameRequest("Cat-Eye Glamour", "Chic angular frame designed to highlight cheekbones.", 139.0, "In Stock", 4.7, 85, "glass1", 12));
        }
    }

    public static class AiResponse {
        public String replyText;
        public FrameRequest matchedFrame;

        public AiResponse(String replyText, FrameRequest matchedFrame) {
            this.replyText = replyText;
            this.matchedFrame = matchedFrame;
        }
    }

    public AiResponse processQuery(String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return new AiResponse("Hello! I'm your Visionary AI Eyewear Specialist. How can I help you find the perfect frames today?", null);
        }

        String query = userQuery.toLowerCase(Locale.US).trim();

        // 1. Face Shape Recommendations
        if (query.contains("round face") || query.contains("round shape")) {
            FrameRequest frame = findFrameByKeyword("square");
            return new AiResponse("For a round face, rectangular or square frames create a striking contrast that sharpens soft features! I recommend our **Classic Square Elite**.", frame);
        }

        if (query.contains("square face") || query.contains("angular face")) {
            FrameRequest frame = findFrameByKeyword("aviator");
            return new AiResponse("For a square face with strong jawlines, round or aviator frames help soften angular angles! Check out our **Aviator Legend**.", frame);
        }

        if (query.contains("oval face") || query.contains("oval shape")) {
            FrameRequest frame = findFrameByKeyword("tortoise");
            return new AiResponse("Lucky you! Oval faces look fantastic in almost any frame shape. A bold design like our **Retro Tortoiseshell** will look stunning on you.", frame);
        }

        if (query.contains("heart face") || query.contains("cat eye") || query.contains("cat-eye")) {
            FrameRequest frame = findFrameByKeyword("cat-eye");
            if (frame == null) frame = findFrameByKeyword("glass1");
            return new AiResponse("Heart-shaped faces look amazing with frames that flair out slightly at the top, like Cat-Eye or light metal frames!", frame);
        }

        // 2. Direct Frame / Style Search
        if (query.contains("square") || query.contains("box")) {
            FrameRequest frame = findFrameByKeyword("square");
            return new AiResponse("Here is one of our best-selling square frames! You can try it on virtually or view it in the catalog.", frame);
        }

        if (query.contains("aviator") || query.contains("metal") || query.contains("pilot")) {
            FrameRequest frame = findFrameByKeyword("aviator");
            return new AiResponse("Here is our top metal aviator frame. Sleek, lightweight, and super durable!", frame);
        }

        if (query.contains("tortoise") || query.contains("brown") || query.contains("vintage")) {
            FrameRequest frame = findFrameByKeyword("tortoise");
            return new AiResponse("Our Tortoiseshell collection offers warm, rich tones with a timeless classic design.", frame);
        }

        if (query.contains("blue light") || query.contains("screen") || query.contains("computer")) {
            FrameRequest frame = findFrameByKeyword("square");
            return new AiResponse("Protect your eyes from digital screen glare! All our frames can be upgraded with Anti-Blue Light anti-glare lenses during checkout.", frame);
        }

        // 3. Price / Budget Search
        if (query.contains("cheap") || query.contains("affordable") || query.contains("budget") || query.contains("under 150") || query.contains("100")) {
            FrameRequest cheapest = getCheapestFrame();
            return new AiResponse("Looking for great value? Our **" + cheapest.getName() + "** offers premium build quality at just $" + String.format(Locale.US, "%.2f", cheapest.getPrice()) + "!", cheapest);
        }

        if (query.contains("best") || query.contains("popular") || query.contains("top rated") || query.contains("recommend")) {
            FrameRequest top = getTopRatedFrame();
            return new AiResponse("Our highest-rated customer favorite is the **" + top.getName() + "** with a " + top.getRating() + "⭐ rating!", top);
        }

        // 4. Feature / App Guidance
        if (query.contains("try on") || query.contains("try-on") || query.contains("camera") || query.contains("face")) {
            return new AiResponse("You can try on any frame live using your camera! Tap the **Virtual Face Try-On** card on the main screen to experience AI try-on.", null);
        }

        if (query.contains("order") || query.contains("track") || query.contains("status") || query.contains("shipping")) {
            return new AiResponse("You can track your order's real-time journey (Placed -> Quality Check -> Shipped -> Delivered) under the **My Orders** section on your home screen!", null);
        }

        if (query.contains("prescription") || query.contains("report") || query.contains("doctor") || query.contains("lens")) {
            return new AiResponse("You can upload your eye prescription report directly using the **Upload Vision Report** card before checkout!", null);
        }

        if (query.contains("vision care") || query.contains("store") || query.contains("location") || query.contains("eye check")) {
            return new AiResponse("Need an eye checkup? Tap **Find the Vision Care Location** on your home page to see centers near you and view them on Google Maps!", null);
        }

        // 5. Generic Search Match
        for (FrameRequest f : catalogFrames) {
            if (query.contains(f.getName().toLowerCase(Locale.US))) {
                return new AiResponse("Yes! We have the **" + f.getName() + "** available for $" + String.format(Locale.US, "%.2f", f.getPrice()) + ". Here are the details:", f);
            }
        }

        // Default Helpful Response
        FrameRequest defaultFrame = catalogFrames.isEmpty() ? null : catalogFrames.get(0);
        return new AiResponse("I can help you find frames for your face shape, search by price or style, or assist with Virtual Try-On! Here is one of our trending styles:", defaultFrame);
    }

    private FrameRequest findFrameByKeyword(String keyword) {
        for (FrameRequest f : catalogFrames) {
            if (f.getName().toLowerCase(Locale.US).contains(keyword) || 
                f.getImageUrl().toLowerCase(Locale.US).contains(keyword) ||
                f.getDescription().toLowerCase(Locale.US).contains(keyword)) {
                return f;
            }
        }
        return catalogFrames.isEmpty() ? null : catalogFrames.get(0);
    }

    private FrameRequest getCheapestFrame() {
        if (catalogFrames.isEmpty()) return null;
        FrameRequest cheapest = catalogFrames.get(0);
        for (FrameRequest f : catalogFrames) {
            if (f.getPrice() < cheapest.getPrice()) {
                cheapest = f;
            }
        }
        return cheapest;
    }

    private FrameRequest getTopRatedFrame() {
        if (catalogFrames.isEmpty()) return null;
        FrameRequest top = catalogFrames.get(0);
        for (FrameRequest f : catalogFrames) {
            if (f.getRating() > top.getRating()) {
                top = f;
            }
        }
        return top;
    }
}
