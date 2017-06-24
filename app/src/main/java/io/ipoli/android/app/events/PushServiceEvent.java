package io.ipoli.android.app.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/8/16.
 */
public class PushServiceEvent {

    public final Exception exception;

    public PushServiceEvent(Exception exception) {
        this.exception = exception;
    }
}
