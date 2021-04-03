package com.sergiocruz.matematica;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */


/*
 *
 *   Hacker Rank tests
 *
 **/
public class UnitTests {

    @Test
    public int[] gradingStudents(final int[] grades) {
        final int[] result = new int[grades.length - 1];

        for (int i = 1; i < grades.length; i++) {
            if (grades[i] >= 38) {

                int nextMultipleOf5 = 0;
                if (grades[i] % 10 < 5) {
                    nextMultipleOf5 = grades[i] - grades[i] % 10 + 5;
                } else if (grades[i] % 10 > 5) {
                    nextMultipleOf5 = grades[i] / 10 * 10 + 10;
                }

                if (nextMultipleOf5 != 0 && Math.abs(nextMultipleOf5 - grades[i]) < 3) {
                    grades[i] = nextMultipleOf5; // adjust grade
                }


            }
        }


        for (int i = 0; i < result.length; i++) {
            result[i] = grades[i + 1];
        }


        return result;
    }


    @Test
    public void gradingTest() {

        final int[] grades = {4, 73, 67, 38, 33};

        final int[] result = this.gradingStudents(grades);

        System.out.println(result.toString());
    }

    @Test
    public void timeConversion() {

        final String string = "12:23:12PM";

        final String ampm = string.substring(string.length() - 2);
        String time = string.substring(0, string.length() - 2);
        final String hours = string.substring(0, 2);
        final int hour = Integer.parseInt(hours);
        final String hourString;

        if (ampm.equalsIgnoreCase("am")) {
            if (hour == 12) {
                hourString = "00";
                time = time.replace(hours, String.valueOf(hourString));
            }

            System.out.println(time);

        } else {

            if (hour == 0) {
                hourString = "00";
            } else if (hour == 12) {
                hourString = String.valueOf(hour);
            } else {
                hourString = String.valueOf(hour + 12);
            }

            time = time.replace(hours, hourString);
            System.out.println(time);
        }


    }

    @Test
    public void candles() {

        final int[] arr = {5, 7, 9, 1, 9};

        final int length = arr.length;
        long max = arr[length - 1];
        for (int i = 0; i < length; i++) {
            if (arr[i] > max)
                max = arr[i];
        }

        int counter = 0;
        for (int i = 0; i < length; i++) {
            if (arr[i] == max)
                counter++;
        }


        System.out.println("counter " + counter);

    }


    @Test
    public void miniMaxSum() {

        final int[] arr = {5, 5, 5, 5, 5, 5};

        final int length = arr.length;
        long min = arr[0];
        long max = arr[length - 1];
        for (int i = 0; i < length; i++) {

            if (arr[i] > max) {
                max = arr[i];
            }
            if (arr[i] < min) {
                min = arr[i];
            }

        }

        //max sum
        long maxSum = 0;
        int excludeIndex = 0;
        for (int i = 0; i < length; i++) {
            if (arr[i] == min) {
                excludeIndex = i;
                break;
            }
        }
        for (int i = 0; i < length; i++) {
            if (i != excludeIndex)
                maxSum += arr[i];
        }

        //min sum
        long minSum = 0;
        excludeIndex = 0;
        for (int i = 0; i < length; i++) {
            if (arr[i] == max) {
                excludeIndex = i;
                break;
            }
        }
        for (int i = 0; i < length; i++) {
            if (i != excludeIndex)
                minSum += arr[i];
        }


        System.out.println("min " + min + " max " + max);
        System.out.println("minsum " + minSum + " maxSum " + maxSum);


    }


}