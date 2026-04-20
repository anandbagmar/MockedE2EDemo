package io.mockede2edemo.e2e.screen.web;

import io.mockede2edemo.e2e.screen.ScreenShotScreen;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

public class ScreenShotScreenWeb extends ScreenShotScreen {
    public ScreenShotScreenWeb(Driver driver, Visual visually) {
    }

    @Override
    public ScreenShotScreen takeScreenshot() {
        return this;
    }
}
