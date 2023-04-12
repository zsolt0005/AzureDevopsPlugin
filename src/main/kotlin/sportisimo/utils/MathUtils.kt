package sportisimo.utils

/**
 * Mathematical functions.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
object MathUtils
{
    /**
     * Clamps the given value in between the given min and max values.
     *
     * @param value The value to be clamped.
     * @param min   Min value.
     * @param max   Max value
     * @return Clamped value.
     */
    fun clamp(value: Int, min: Int, max: Int): Int
    {
        return if(value > max) max
          else if(value < min) min
          else value
    }
}