package jota.utils;

import java.text.DecimalFormat;

/**
 * This class provides methods to convert Cvt to different units.
 *
 * @author sascha
 */
public class CvtUnitConverter {

    /**
     * Convert the cvt amount.
     *
     * @param amount   The amount.
     * @param fromUnit The source unit e.g. the unit of amount.
     * @param toUnit   The target unit.
     * @return The specified amount in the target unit.
     **/
    public static long convertUnits(long amount, CvtUnits fromUnit, CvtUnits toUnit) {
        long amountInSource = (long) (amount * Math.pow(10, fromUnit.getValue()));
        return convertUnits(amountInSource, toUnit);
    }



    /**
     * Convert unit.
     *
     * @param amount The amount.
     * @param toUnit The target unit.
     * @return The specified amount in the target unit.
     **/
    private static long convertUnits(long amount, CvtUnits toUnit) {
        return (long) (amount / Math.pow(10, toUnit.getValue()));
    }

    /**
     * Convert the cvt amount to text.
     *
     * @param amount   The amount.
     * @param extended Extended length.
     * @return The specified amount in the target unit.
     **/
    public static String convertRawCvtAmountToDisplayText(long amount, boolean extended) {
        CvtUnits unit = findOptimalCvtUnitToDisplay(amount);
        double amountInDisplayUnit = convertAmountTo(amount, unit);
        return createAmountWithUnitDisplayText(amountInDisplayUnit, unit, extended);
    }


    /**
     * Convert amount to target unit.
     *
     * @param amount The amount.
     * @return The target unit.
     **/
    public static double convertAmountTo(long amount, CvtUnits target) {
        return amount / Math.pow(10, target.getValue());
    }

    /**
     * Create amount with unit text.
     *
     * @param amountInUnit The amount in units.
     * @param unit         The unit.
     * @param extended     Extended length.
     * @return The target unit.
     **/
    private static String createAmountWithUnitDisplayText(double amountInUnit, CvtUnits unit, boolean extended) {
        String result = createAmountDisplayText(amountInUnit, unit, extended);
        result += " " + unit.getUnit();
        return result;
    }


    /**
     * Create amount text.
     *
     * @param amountInUnit The amount in units.
     * @param unit         The unit.
     * @param extended     Extended length.
     * @return The target unit.
     **/
    public static String createAmountDisplayText(double amountInUnit, CvtUnits unit, boolean extended) {
        DecimalFormat df;
        if (extended) df = new DecimalFormat("##0.##################");
        else
            df = new DecimalFormat("##0.##");

        String result = "";
        // display unit as integer if value is between 1-999 or in decimal format
        result += unit == CvtUnits.CVT ? (long) amountInUnit : df.format(amountInUnit);
        return result;
    }







}
