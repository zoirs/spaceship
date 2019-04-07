package ru.chernyshev.control.type;

import org.apache.logging.log4j.util.Strings;

/**
 * Список параметров спутника
 */
public enum ConfigurationParam {

    RADIO_POWER_DBM("radioPowerDbm", 20, 80, false),
    COOLING_SYSTEM_POWER_PCT("coolingSystemPowerPct", 0, 100, false),
    MAIN_ENGINE_THRUST_PCT("mainEngineThrustPct", 0, 100, false),
    ORIENTATION_ZENITH_ANGLE_DEG("orientationZenithAngleDeg", 0, 359, true),
    ORIENTATION_AZIMUTH_ANGLE_DEG("orientationAzimuthAngleDeg", 0, 359, true),
    VESSEL_ALTITUDE_M("vesselAltitudeM", 0, 35_000_000, true),
    VESSEL_SPEED_MPS("vesselSpeedMps", 0, 15000, true),
    MAIN_ENGINE_FUEL_PCT("mainEngineFuelPct", 0, 100, true),
    TEMPERATURE_INTERNAL_DEG("temperatureInternalDeg", -50, 150, true),
    ;

    /**
     * Наименование параметра
     */
    private final String key;

    /**
     * Максимально возможное значение
     */
    private final int max;

    /**
     * Минимально возможное значение
     */
    private final int min;

    /**
     * Необходимоть указания в телеметрии
     */
    private final boolean containsInTelemetry;

    ConfigurationParam(String key, int min, int max, boolean containsInTelemetry) {
        this.key = key;
        this.min = min;
        this.max = max;
        this.containsInTelemetry = containsInTelemetry;
    }

    /**
     * @return true если параметр необходимо передавать в телеметрии
     */
    public boolean isContainsInTelemetry() {
        return containsInTelemetry;
    }

    /**
     * @param key   наименование параметра
     * @param value значение параметра
     * @return true если значение параметра валидно
     */
    public static boolean isValid(String key, Integer value) {
        if (value == null) {
            return false;
        }
        ConfigurationParam param = getValueFor(key);
        if (param == null) {
            return false;
        }
        if (param.min > value) {
            return false;
        }
        return param.max >= value;
    }

    /**
     * @return наименование параметра
     */
    public String getKey() {
        return key;
    }

    private static ConfigurationParam getValueFor(String key) {
        if (Strings.isEmpty(key)) {
            return null;
        }

        for (ConfigurationParam confParam : values()) {
            if (key.equals(confParam.key)) {
                return confParam;
            }
        }
        return null;
    }
}
