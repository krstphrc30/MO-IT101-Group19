/*
 * Click nbfs://SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 /**
 *
 * @author Kristopher Carlo, Pil Antony, Janice, Rey
 */

package com.group19.motorph;

import com.opencsv.CSVReader;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class MotorPH {
    
    private static final double OVERTIME_RATE = 1.25; // Overtime pay multiplier
    private static final int WEEKLY_PAYMENTS = 4; // Number of weeks in a month
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    
    public static void main(String[] args) {
        String employeeFilePath = "src/main/employeedata.csv";
        String attendanceFilePath = "src/main/attendancerecord.csv";

        Scanner inputScanner = new Scanner(System.in);
        boolean validInput = false;
        
        while (!validInput) {
        System.out.println("Welcome to MotorPH Payroll!");
        System.out.println("[1] Compute Payroll for a Specific Employee");
        System.out.println("[2] Compute Payroll for All Employees");
        System.out.print("Enter selection: ");
        
        // Check if input is an integer
    if (inputScanner.hasNext()) {
        int choice = inputScanner.nextInt();
        inputScanner.nextLine(); // Consume newline
        
        switch (choice) {
            case 1 -> {
                System.out.print("\nEnter Employee Number: ");
                String searchEmployeeNumber = inputScanner.nextLine().trim();

                if (searchEmployeeNumber.isEmpty()) {
                    System.out.println("No input provided. Please try again.");
                    continue; // Loop back to selection
                }
                processPayrollForEmployee(searchEmployeeNumber, employeeFilePath, attendanceFilePath);
                validInput = true; // Exit loop after successful processing
            }
            case 2 -> {
                processPayrollForAllEmployees(employeeFilePath, attendanceFilePath);
                validInput = true; // Exit loop after successful processing
            }
            default -> System.out.println("Invalid choice. Please eneter 1 or 2.");
            }
        } else {
            System.out.println("Invalid input. Please enter a number (1 or 2");
            inputScanner.nextLine();
        }
    }
        inputScanner.close();
    }
    
    private static void processPayrollForEmployee(String employeeNumber, String employeeFilePath, String attendanceFilePath) {
        try (CSVReader reader = new CSVReader(new FileReader(employeeFilePath))) {
            reader.readNext(); // Skip header
            String[] employeeData;
            while ((employeeData = reader.readNext()) != null) {
                if (employeeData.length >= 19 && employeeData[0].trim().equals(employeeNumber)) {
                    String fullName = employeeData[1].trim() + ", " + employeeData[2].trim();
                    String birthDay = employeeData[3].trim();
                    double hourlyRate = Double.parseDouble(employeeData[18].trim().replaceAll("\"", "").replace(",", ""));
                    double riceSubsidy = Double.parseDouble(employeeData[14].trim().replaceAll("\"", "").replace(",", ""));
                    double phoneAllowance = Double.parseDouble(employeeData[15].trim().replaceAll("\"", "").replace(",", ""));
                    double clothingAllowance = Double.parseDouble(employeeData[16].trim().replaceAll("\"", "").replace(",", ""));
                    System.out.println("-------------------------------------------------");
                    System.out.println("Name: " + fullName);
                    System.out.println("Birthday: " + birthDay);
                    System.out.println("-------------------------------------------------");
                    calculateWeeklyWorkHours(employeeNumber, fullName, hourlyRate, riceSubsidy, phoneAllowance, clothingAllowance, attendanceFilePath);
                    return;
                }
            }
            System.out.println("Employee with ID " + employeeNumber + " not found.");
        } catch (Exception e) {
            System.out.println("Error reading employee file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processPayrollForAllEmployees(String employeeFilePath, String attendanceFilePath) {
        try (CSVReader reader = new CSVReader(new FileReader(employeeFilePath))) {
            reader.readNext(); // Skip header
            String[] employeeData;
            while ((employeeData = reader.readNext()) != null) {
                if (employeeData.length >= 19) {
                    String employeeNumber = employeeData[0].trim();
                    String fullName = employeeData[1].trim() + ", " + employeeData[2].trim();
                    String birthDay = employeeData[3].trim();
                    double hourlyRate = Double.parseDouble(employeeData[18].trim().replaceAll("\"", "").replace(",", ""));
                    double riceSubsidy = Double.parseDouble(employeeData[14].trim().replaceAll("\"", "").replace(",", ""));
                    double phoneAllowance = Double.parseDouble(employeeData[15].trim().replaceAll("\"", "").replace(",", ""));
                    double clothingAllowance = Double.parseDouble(employeeData[16].trim().replaceAll("\"", "").replace(",", ""));
                    System.out.println("-------------------------------------------------");
                    System.out.println("Employee Number: " + employeeNumber);
                    System.out.println("Name: " + fullName);
                    System.out.println("Birthday: " + birthDay);
                    System.out.println("-------------------------------------------------");
                    calculateWeeklyWorkHours(employeeNumber, fullName, hourlyRate, riceSubsidy, phoneAllowance, clothingAllowance, attendanceFilePath);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading employee file: " + e.getMessage());
            e.printStackTrace();
        }
    }
/**
     * Orchestrates the payroll calculation process for an employee.
     */
    private static void calculateWeeklyWorkHours(String employeeId, String fullName, double hourlyRate,
                                                 double riceSubsidy, double phoneAllowance, double clothingAllowance,
                                                 String attendanceFilePath) {
        TreeMap<LocalDate, Duration[]> weeklyRecords = processAttendanceData(employeeId, attendanceFilePath);
        if (weeklyRecords.isEmpty()) {
            System.out.println("No valid attendance records found for " + fullName);
            return;
        }
        calculateAndDisplayPayroll(fullName, weeklyRecords, hourlyRate, riceSubsidy, phoneAllowance, clothingAllowance);
    }

    /**
     * Processes attendance data and returns weekly work and overtime durations.
     */
    private static TreeMap<LocalDate, Duration[]> processAttendanceData(String employeeId, String attendanceFilePath) {
        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime graceEnd = workStart.plusMinutes(10);
        LocalTime workEnd = LocalTime.of(17, 0);
        Duration breakTime = Duration.ofHours(1);
        TreeMap<LocalDate, Duration[]> weeklyRecords = new TreeMap<>();

        try (CSVReader reader = new CSVReader(new FileReader(attendanceFilePath))) {
            reader.readNext(); // Skip header
            String[] attendanceRecord;
            while ((attendanceRecord = reader.readNext()) != null) {
                if (attendanceRecord.length >= 6 && attendanceRecord[0].trim().equals(employeeId)) {
                    processSingleAttendanceRecord(attendanceRecord, weeklyRecords, workStart, graceEnd, workEnd, breakTime);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
            e.printStackTrace();
        }
        return weeklyRecords;
    }

    /**
     * Processes a single attendance record and updates weekly durations.
     */
    private static void processSingleAttendanceRecord(String[] attendanceRecord, TreeMap<LocalDate, Duration[]> weeklyRecords,
                                                      LocalTime workStart, LocalTime graceEnd, LocalTime workEnd, Duration breakTime) {
        String dateStr = attendanceRecord[3].trim();
        String logInStr = attendanceRecord[4].trim();
        String logOutStr = attendanceRecord[5].trim();

        if (logInStr.isEmpty() || logOutStr.isEmpty()) return;

        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) return;

            LocalTime logIn = LocalTime.parse(logInStr, TIME_FORMATTER);
            LocalTime logOut = LocalTime.parse(logOutStr, TIME_FORMATTER);

            if (logOut.isBefore(logIn)) {
                System.out.println("Error: Invalid time record for " + attendanceRecord[0] + " on " + dateStr);
                return;
            }

            boolean isLate = logIn.isAfter(graceEnd);
            if (logIn.isAfter(workStart) && logIn.isBefore(graceEnd)) logIn = workStart;

            LocalTime adjustedLogOut = logOut.isAfter(workEnd) ? workEnd : logOut;
            Duration workDuration = Duration.between(logIn, adjustedLogOut).minus(breakTime);
            workDuration = workDuration.isNegative() ? Duration.ZERO : workDuration;

            Duration overtimeDuration = (!isLate && logOut.isAfter(workEnd)) ? Duration.between(workEnd, logOut) : Duration.ZERO;

            LocalDate weekStart = date.with(DayOfWeek.MONDAY);
            weeklyRecords.putIfAbsent(weekStart, new Duration[]{Duration.ZERO, Duration.ZERO});
            Duration[] durations = weeklyRecords.get(weekStart);
            durations[0] = durations[0].plus(workDuration);
            durations[1] = durations[1].plus(overtimeDuration);
        } catch (DateTimeParseException e) {
            System.out.println("Error parsing attendance date/time for " + attendanceRecord[0] + " on " + dateStr);
        }
    }

    /**
     * Calculates payroll and displays the summary.
     */
    private static void calculateAndDisplayPayroll(String fullName, TreeMap<LocalDate, Duration[]> weeklyRecords,
                                                   double hourlyRate, double riceSubsidy, double phoneAllowance,
                                                   double clothingAllowance) {
        System.out.println("\nWeekly Salary Summary for " + fullName + ":");
        System.out.println("-------------------------------------------------");

        for (Map.Entry<LocalDate, Duration[]> entry : weeklyRecords.entrySet()) {
            LocalDate startOfWeek = entry.getKey();
            LocalDate endOfWeek = startOfWeek.plusDays(4);
            Duration workDuration = entry.getValue()[0];
            Duration overtimeDuration = entry.getValue()[1];

            double baseSalary = workDuration.toMinutes() / 60.0 * hourlyRate;
            double overtimePay = overtimeDuration.toMinutes() / 60.0 * hourlyRate * OVERTIME_RATE;
            double grossSalary = baseSalary + overtimePay;
            double totalAllowances = (riceSubsidy + phoneAllowance + clothingAllowance) / WEEKLY_PAYMENTS;

            // Calculate deductions using helper functions
            double sssContribution = calculateSSSContribution(grossSalary);
            double philHealthContribution = calculatePhilHealthContribution(grossSalary);
            double pagIbigContribution = calculatePagIbigContribution(grossSalary);
            double withholdingTax = calculateWithholdingTax(grossSalary);
            double totalDeductions = sssContribution + philHealthContribution + pagIbigContribution;

            double netSalary = grossSalary - totalDeductions - withholdingTax;
            double finalPay = netSalary + totalAllowances;

            printPayrollDetails(startOfWeek, endOfWeek, workDuration, overtimeDuration, baseSalary, overtimePay,
                    grossSalary, sssContribution, philHealthContribution, pagIbigContribution, totalDeductions,
                    withholdingTax, totalAllowances, finalPay);
        }
    }

    /**
     * Calculates SSS contribution based on gross salary.
     */
    private static double calculateSSSContribution(double grossSalary) {
        class SalaryContribution {
            double salaryLimit;
            double contribution;

            SalaryContribution(double salaryLimit, double contribution) {
                this.salaryLimit = salaryLimit;
                this.contribution = contribution;
            }
        }

        ArrayList<SalaryContribution> sssTable = new ArrayList<>();
        sssTable.add(new SalaryContribution(3250.0 / 4, 135.00));
        sssTable.add(new SalaryContribution(3750.0 / 4, 157.50));
        sssTable.add(new SalaryContribution(4250.0 / 4, 180.00));
        sssTable.add(new SalaryContribution(4750.0 / 4, 202.50));
        sssTable.add(new SalaryContribution(5250.0 / 4, 225.00));
        sssTable.add(new SalaryContribution(5750.0 / 4, 247.50));
        sssTable.add(new SalaryContribution(6250.0 / 4, 270.00));
        sssTable.add(new SalaryContribution(6750.0 / 4, 292.50));
        sssTable.add(new SalaryContribution(7250.0 / 4, 315.00));
        sssTable.add(new SalaryContribution(7750.0 / 4, 337.50));
        sssTable.add(new SalaryContribution(8250.0 / 4, 360.00));
        sssTable.add(new SalaryContribution(8750.0 / 4, 382.50));
        sssTable.add(new SalaryContribution(9250.0 / 4, 405.00));
        sssTable.add(new SalaryContribution(9750.0 / 4, 427.50));
        sssTable.add(new SalaryContribution(10250.0 / 4, 450.00));
        sssTable.add(new SalaryContribution(10750.0 / 4, 472.50));
        sssTable.add(new SalaryContribution(11250.0 / 4, 495.00));
        sssTable.add(new SalaryContribution(11750.0 / 4, 517.50));
        sssTable.add(new SalaryContribution(12250.0 / 4, 540.00));
        sssTable.add(new SalaryContribution(12750.0 / 4, 562.50));
        sssTable.add(new SalaryContribution(13250.0 / 4, 585.00));
        sssTable.add(new SalaryContribution(13750.0 / 4, 607.50));
        sssTable.add(new SalaryContribution(14250.0 / 4, 630.00));
        sssTable.add(new SalaryContribution(14750.0 / 4, 652.50));
        sssTable.add(new SalaryContribution(15250.0 / 4, 675.00));
        sssTable.add(new SalaryContribution(15750.0 / 4, 697.50));
        sssTable.add(new SalaryContribution(16250.0 / 4, 720.00));
        sssTable.add(new SalaryContribution(16750.0 / 4, 742.50));
        sssTable.add(new SalaryContribution(17250.0 / 4, 765.00));
        sssTable.add(new SalaryContribution(17750.0 / 4, 787.50));
        sssTable.add(new SalaryContribution(18250.0 / 4, 810.00));
        sssTable.add(new SalaryContribution(18750.0 / 4, 832.50));
        sssTable.add(new SalaryContribution(19250.0 / 4, 855.00));
        sssTable.add(new SalaryContribution(19750.0 / 4, 877.50));
        sssTable.add(new SalaryContribution(20250.0 / 4, 900.00));
        sssTable.add(new SalaryContribution(20750.0 / 4, 922.50));
        sssTable.add(new SalaryContribution(21250.0 / 4, 945.00));
        sssTable.add(new SalaryContribution(21750.0 / 4, 967.50));
        sssTable.add(new SalaryContribution(22250.0 / 4, 990.00));
        sssTable.add(new SalaryContribution(22750.0 / 4, 1012.50));
        sssTable.add(new SalaryContribution(23250.0 / 4, 1035.00));
        sssTable.add(new SalaryContribution(23750.0 / 4, 1057.50));
        sssTable.add(new SalaryContribution(24250.0 / 4, 1080.00));
        sssTable.add(new SalaryContribution(24750.0 / 4, 1102.50));

        double sssContribution = 0.0;
        for (SalaryContribution sssEntry : sssTable) {
            if (grossSalary <= sssEntry.salaryLimit) {
                sssContribution = sssEntry.contribution / WEEKLY_PAYMENTS;
                break;
            }
        }
        return (sssContribution == 0.0) ? 1125.00 / WEEKLY_PAYMENTS : sssContribution;
    }

    /**
     * Calculates PhilHealth contribution based on gross salary.
     */
    private static double calculatePhilHealthContribution(double grossSalary) {
        double monthlyPremium = ((grossSalary * 0.03) / 2) / (4); // 3% of salary
        if (grossSalary <= 10000 / 4) return 300 / 2 / WEEKLY_PAYMENTS; // Employee's share, weekly
        else if (grossSalary <= 59999.99 / 4) return monthlyPremium / 2 / WEEKLY_PAYMENTS; // Employee's share, weekly
        else return 1800 / 2 / WEEKLY_PAYMENTS; // Max contribution, weekly
    }

    /**
     * Calculates Pag-IBIG contribution based on gross salary.
     */
    private static double calculatePagIbigContribution(double grossSalary) {
        double contribution = (grossSalary <= 1500) ? grossSalary * 0.01 : grossSalary * 0.02;
        return Math.min(contribution, 100.00 / WEEKLY_PAYMENTS);
    }

    /**
     * Calculates withholding tax based on gross salary.
     */
    private static double calculateWithholdingTax(double grossSalary) {
        if (grossSalary <= 20832 / 4) return 0.0;
        else if (grossSalary <= 33333 / 4) return ((grossSalary - 20833 / 4) * 0.20);
        else if (grossSalary <= 66667 / 4) return (2500 / 4) + ((grossSalary - 33333 / 4) * 0.25);
        else if (grossSalary <= 166667 / 4) return (10833 / 4) + ((grossSalary - 66667 / 4) * 0.30);
        else if (grossSalary <= 666667) return (40833.33 / 4) + ((grossSalary - 166667 / 4) * 0.32);
        else return (208333.33 / 4) + ((grossSalary - 166667) * 0.35);
    }

    /**
     * Prints payroll details for a given week.
     */
    private static void printPayrollDetails(LocalDate startOfWeek, LocalDate endOfWeek, Duration workDuration,
                                            Duration overtimeDuration, double baseSalary, double overtimePay,
                                            double grossSalary, double sssContribution, double philHealthContribution,
                                            double pagIbigContribution, double totalDeductions, double withholdingTax,
                                            double totalAllowances, double finalPay) {
        System.out.println("Week Period             : " + startOfWeek + " - " + endOfWeek);
        System.out.println("Total Hours Worked      : " + workDuration.toHours() + "h " + workDuration.toMinutesPart() + "m");
        System.out.println("Total Overtime          : " + overtimeDuration.toHours() + "h " + overtimeDuration.toMinutesPart() + "m");
        System.out.println("Base Salary             : PHP " + String.format("%.2f", baseSalary));
        System.out.println("Overtime Pay            : PHP " + String.format("%.2f", overtimePay));
        System.out.println("Gross Salary            : PHP " + String.format("%.2f", grossSalary));
        System.out.println("SSS Contribution        : PHP " + String.format("-%.2f", sssContribution));
        System.out.println("PhilHealth Contribution : PHP " + String.format("-%.2f", philHealthContribution));
        System.out.println("Pag-Ibig Contribution   : PHP " + String.format("-%.2f", pagIbigContribution));
        System.out.println("Total Deductions        : PHP " + String.format("-%.2f", totalDeductions));
        System.out.println("Withholding Tax         : PHP " + String.format("-%.2f", withholdingTax));
        System.out.println("Allowances              : PHP " + String.format("%.2f", totalAllowances));
        System.out.println("\nNet Salary              : PHP " + String.format("%.2f", finalPay));
        System.out.println("--------------------------------------------------");
    }
}