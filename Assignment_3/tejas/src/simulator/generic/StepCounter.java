package generic;

/**
 * Created by anand.j on 4/13/17.
 */
public class StepCounter extends StatCounter {

    private long start;

    public void start(long start) {
        super.start();

        this.start = start;
    }

    public void stop(long end) {
        inc(end - this.start);

        super.stop();
    }

}
