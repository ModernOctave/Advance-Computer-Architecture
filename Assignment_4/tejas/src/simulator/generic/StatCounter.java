package generic;

/**
 * Created by anand.j on 4/13/17.
 */
public abstract class StatCounter {

    protected Boolean on;

    protected long count;
    protected long temp;

    public void start() {
        this.on = true;
        this.temp = 0;
    }

    public void stop() {
        if (this.on == null || this.on) {
            this.count += this.temp;
        }
        this.on = false;
        this.temp = 0;
    }

    public void inc(long inc) {
        this.temp += inc;
    }

    public long getCount() {
        return this.count;
    }

}
