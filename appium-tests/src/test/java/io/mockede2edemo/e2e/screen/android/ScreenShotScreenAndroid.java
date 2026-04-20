package io.mockede2edemo.e2e.screen.android;

import io.mockede2edemo.e2e.screen.ScreenShotScreen;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

public class ScreenShotScreenAndroid extends ScreenShotScreen {
    public ScreenShotScreenAndroid(Driver driver, Visual visually) {
    }

    @Override
    public ScreenShotScreen takeScreenshot() {
        return this;
    }
}
