package keqing.gtqt.prismplan.api.capability;

import appeng.api.config.Actionable;

public interface IEnergyHatch {
    // 能量操作
    double injectPower(double amount, Actionable mode);
    double extractPower(double amount, Actionable mode);

    // 能量状态
    double getEnergyStored();
    double getMaxEnergyStore();

    // 剩余容量计算
    default double getRemainingCapacity() {
        return getMaxEnergyStore() - getEnergyStored();
    }

    // 容量重计算相关
    default boolean shouldRecalculateCap() {
        return false;
    }

    default void recalculateCapacity() {}

    // 其他必要方法
    default double getIdleDrain() {
        return 0.0;
    }

}