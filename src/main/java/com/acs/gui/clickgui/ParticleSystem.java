package com.acs.gui.clickgui;

import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleSystem {
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    public ParticleSystem(int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle());
        }
    }

    public void render(DrawContext context, int width, int height) {
        for (Particle p : particles) {
            p.update(width, height);
            context.fill((int)p.x, (int)p.y, (int)p.x + 1, (int)p.y + 1, 0x88FFFFFF);
        }
    }

    private class Particle {
        float x, y, vx, vy;

        Particle() {
            reset();
        }

        void reset() {
            x = random.nextInt(2000);
            y = random.nextInt(2000);
            vx = (random.nextFloat() - 0.5f) * 0.5f;
            vy = (random.nextFloat() - 0.5f) * 0.5f;
        }

        void update(int width, int height) {
            x += vx;
            y += vy;
            if (x < 0 || x > width || y < 0 || y > height) reset();
        }
    }
}
