package io.mockede2edemo.e2e.screen.windows;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import io.mockede2edemo.e2e.screen.ScreenShotScreen;

public class ScreenShotScreenWindows extends ScreenShotScreen {
    public ScreenShotScreenWindows(Driver driver, Visual visually) {
    }

    @Override
    public ScreenShotScreen takeScreenshot() {
        return this;
    }
}
