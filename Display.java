import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class Display extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;
	private double fpsCap = 60d;
    private long nextStartTime;
    private int fps, ups;
    private final BufferedImage img;
    private final int width, height;
    private final String title;
    private final Canvas canvas;
    private boolean showFpsUps;

    public Display(int width, int height, String title){
        this.width = width;
        this.height = height;
        this.title = title;
        if(Compiler.fullscreen){
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        setPreferredSize(new Dimension(width, height));
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(width, height));
        add(canvas);
        pack();
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        setVisible(true);
    }

    private void loop() {
        double acc = 0;
        long currentT, lastU = System.currentTimeMillis();
        nextStartTime = System.currentTimeMillis() + 1000;
        if(!Compiler.customLoop) {
            while (true) {
                currentT = System.currentTimeMillis();
                double lastRenderTS = (currentT - lastU) / 1000d;
                acc += lastRenderTS;
                lastU = currentT;
                double updateRate = 1.0d / fpsCap;
                if (acc >= updateRate) {
                    while (acc > updateRate) {
                        _update();
                        acc -= updateRate;
                    }
                    render();
                }
                printStats();
            }
        }
        noLoop();
    }

    public void render(){
        BufferStrategy bs = canvas.getBufferStrategy();
        if(bs == null) {
            canvas.createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        g.drawImage(img, 0, 0, null);
        bs.show();
        fps++;
    }

    private void _update(){
        ups++;
    }

    public void update(){

    }

    public void addRect(int x, int y, int width, int height, int rgb){
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                img.setRGB(j, i, rgb);
            }
        }
    }

    public void setFps(int fps) {
        this.fpsCap = fps;
    }

    public void noLoop() {
        try {
            while (true) {
                Thread.sleep(1000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        loop();
    }

    public void setShowFpsUps(boolean show) {
        showFpsUps = show;
    }

    private void printStats() {
        if (System.currentTimeMillis() > nextStartTime) {
            if (showFpsUps) {
                System.out.printf("FPS: %d, UPS: %d%n", fps, ups);
            }
            fps = 0;
            ups = 0;
            nextStartTime = System.currentTimeMillis() + 1000;
        }
    }

    public int getColor(int x, int y){
        return img.getRGB(x, y);
    }

    public void setColor(int x, int y, int color){
        img.setRGB(x, y, color);
    }

}
