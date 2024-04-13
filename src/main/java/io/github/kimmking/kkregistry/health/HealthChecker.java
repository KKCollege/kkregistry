package io.github.kimmking.kkregistry.health;

/**
 * Interface for health checker.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/4/13 20:41
 */
public interface HealthChecker {

    void start();
    void stop();

}
