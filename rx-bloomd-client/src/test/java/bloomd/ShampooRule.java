package bloomd;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Apply to flaky tests as needed
 * <p>
 * Apache 2 license
 * https://gist.github.com/JakeWharton/7fe7deb1f7f4a795c120
 */
public final class ShampooRule implements TestRule {
    private final int iterations;

    public ShampooRule(int iterations) {
        if (iterations < 1) throw new IllegalArgumentException("iterations < 1: " + iterations);
        this.iterations = iterations;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                for (int i = 0; i < iterations; i++) {
                    base.evaluate();
                }
            }
        };
    }
}