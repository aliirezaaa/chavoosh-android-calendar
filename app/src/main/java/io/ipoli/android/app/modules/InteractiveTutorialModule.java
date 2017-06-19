package io.ipoli.android.app.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import io.ipoli.android.app.tutorial.InteractiveTutorial;

/**
 * Created by Client-9 on 6/17/2017.
 */
@Module
public class InteractiveTutorialModule {
    @Provides
    @Singleton
    public InteractiveTutorial provideInteractiveTutorial() {
        return new InteractiveTutorial();
    }
}
