/*
 * Click nbfs://SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 /**
 *
 * @author Kristopher Carlo, Pil Antony, Janice, Rey
 */

package com.group19.motorph;

import com.opencsv.CSVReader;                           // Library for reading CSV files
import java.io.*;                                       // For file input/output operations
import java.time.*;                                     // For date and time calculations
import java.time.format.DateTimeFormatter;              // For parsing and formatting dates/times
import java.time.format.DateTimeParseException;         // For handling date/time parsing errors
import java.util.*;                                     // For collections like ArrayList and TreeMap

public class MotorPH {
    
    // Constants used throughout the payroll calculations
    private static final double OVERTIME_RATE = 1.25; // Overtime pay rate multiplier (25% above regular rate)
    private static final int WEEKLY_PAYMENTS = 4;     // Number of weeks in a month for converting monthly amounts to weekly
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");      // Date format for parsing CSV dates
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");            // Time format for parsing CSV times (24-hour)
    
    /**
     * Main entry point of the MotorPH payroll system. Displays a menu to the user,
     * allowing them to choose between computing payroll for a specific employee or all employees.
     * Handles user input and initiates the payroll processing accordingly.
     *
     * @param args Command-line arguments (not used in this implementation)
     */
    
    public static void main(String[] args) {
        // Define file paths for employee and attendance data
        String employeeFilePath = "src/main/employeedata.csv";          // Path to CSV containing employee details
        String attendanceFilePath = "src/main/attendancerecord.csv";    // Path to CSV containing attendance records

        Scanner inputScanner = new Scanner(System.in);                  // Scanner object to read user input from console
        boolean validInput = false;                                     // Flag to control the menu loop until valid input is received
        
        // Menu loop to prompt user for payroll processing options
        while (!validInput) {
        System.out.println("Welcome to MotorPH Payroll!");                  // Welcome message
        System.out.println("[1] Compute Payroll for a Specific Employee");  // Option 1
        System.out.println("[2] Compute Payroll for All Employees");        // Option 2
        System.out.print("Enter selection: ");                              // Promp for user input
        
        // Check if input is an integer
    if (inputScanner.hasNext()) {                                           // Read the integer choice
        int choice = inputScanner.nextInt();                                // Consume the newline character after the integer
        inputScanner.nextLine(); // Consume newline
        
        switch (choice) {
            case 1 -> {
                // Option 1: Compute payroll for a specific employee
                System.out.print("\nEnter Employee Number: ");                  // Prompt for employee ID
                String searchEmployeeNumber = inputScanner.nextLine().trim();   // Read and trim the input

                if (searchEmployeeNumber.isEmpty()) {
                    System.out.println("No input provided. Please try again."); // Handle empty input
                    continue; // Loop back to selection
                }
                processPayrollForEmployee(searchEmployeeNumber, employeeFilePath, attendanceFilePath);
                validInput = true; // Exit loop after successful processing
            }
            case 2 -> {
                // Option 2: Compute payroll for all employees
                processPayrollForAllEmployees(employeeFilePath, attendanceFilePath);
                validInput = true; // Exit loop after successful processing
            }
            default -> System.out.println("Invalid choice. Please eneter 1 or 2.");
            }
        } else {
            System.out.println("Invalid input. Please enter a number (1 or 2");
            inputScanner.nextLine();    // Clear the invalid input
        }
    }
        inputScanner.close();           // Close the scanner to free system resources
    }
    /**
     * Processes payroll for a specific employee identified by their employee number.
     * Reads employee data from the CSV file, extracts relevant details, and initiates
     * payroll calculation based on attendance records.
     *
     * @param employeeNumber The ID of the employee to process payroll for
     * @param employeeFilePath Path to the employee data CSV file
     * @param attendanceFilePath Path to the attendance records CSV file
     */
    
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
                    
                    // Display basic employee information
                    System.out.println("-------------------------------------------------");
                    System.out.println("Name: " + fullName);
                    System.out.println("Birthday: " + birthDay);
                    System.out.println("-------------------------------------------------");
                    
                    // Calculate and display payroll based on attendance
                    calculateWeeklyWorkHours(employeeNumber, fullName, hourlyRate, riceSubsidy, phoneAllowance, clothingAllowance, attendanceFilePath);
                    return;
                }
            }
            System.out.println("Employee with ID " + employeeNumber + " not found.");       // Employee not found in CSV
        } catch (Exception e) {
            System.out.println("Error reading employee file: " + e.getMessage());           // Display error message
            e.printStackTrace(); // Print stack trace for debugging
            e.printStackTrace(); // Print stack trace for debugging   
        }
    }

    private static void processPayrollForAllEmployees(String employeeFilePath, String attendanceFilePath) {
        try (CSVReader reader = new CSVReader(new FileReader(employeeFilePath))) {
            reader.readNext(); // Skip the header row
            String[] employeeData;      // Array to hold each row of employee data
            while ((employeeData = reader.readNext()) != null) {                            // Read each row until end of file
                if (employeeData.length >= 19) {                                            // Ensure row has enough columns
                    
                    // Extract employee details
                    String employeeNumber = employeeData[0].trim();
                    String fullName = employeeData[1].trim() + ", " + employeeData[2].trim();
                    String birthDay = employeeData[3].trim();
                    double hourlyRate = Double.parseDouble(employeeData[18].trim().replaceAll("\"", "").replace(",", ""));
                    double riceSubsidy = Double.parseDouble(employeeData[14].trim().replaceAll("\"", "").replace(",", ""));
                    double phoneAllowance = Double.parseDouble(employeeData[15].trim().replaceAll("\"", "").replace(",", ""));
                    double clothingAllowance = Double.parseDouble(employeeData[16].trim().replaceAll("\"", "").replace(",", ""));
                    
                    // Display employee information (employee number, full name and birtday)
                    System.out.println("-------------------------------------------------");
                    System.out.println("Employee Number: " + employeeNumber);
                    System.out.println("Name: " + fullName);
                    System.out.println("Birthday: " + birthDay);
                    System.out.println("-------------------------------------------------");
                    
                    // Calculate and display payroll for this employee
                    calculateWeeklyWorkHours(employeeNumber, fullName, hourlyRate, riceSubsidy, phoneAllowance, clothingAllowance, attendanceFilePath);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading employee file: " + e.getMessage());           // Display error message
            e.printStackTrace(); // Print stack trace for debugging
        }
    }
/**
     * Orchestrates the payroll calculation process for an employee by processing their
     * attendance data and displaying the payroll summary. This method acts as a bridge
     * between attendance processing and payroll calculation/display.
     *
     * @param employeeId Employee's ID number
     * @param fullName Employee's full name
     * @param hourlyRate Employee's hourly pay rate
     * @param riceSubsidy Monthly rice subsidy amount
     * @param phoneAllowance Monthly phone allowance amount
     * @param clothingAllowance Monthly clothing allowance amount
     * @param attendanceFilePath Path to the attendance records CSV file
     */
    
    private static void calculateWeeklyWorkHours(String employeeId, String fullName, double hourlyRate,
                                                 double riceSubsidy, double phoneAllowance, double clothingAllowance,
                                                 String attendanceFilePath) {
        TreeMap<LocalDate, Duration[]> weeklyRecords = processAttendanceData(employeeId, attendanceFilePath);       // Process attendance data from attendance CSV file.
        if (weeklyRecords.isEmpty()) {
            System.out.println("No valid attendance records found for " + fullName);        // No records found
            return;
        }
        calculateAndDisplayPayroll(fullName, weeklyRecords, hourlyRate, riceSubsidy, phoneAllowance, clothingAllowance);        // Calculate and display payroll
    }

    /**
     * Processes attendance data from the CSV file for a given employee and organizes it
     * into weekly records of regular work hours and overtime hours.
     *
     * @param employeeId Employee's ID number
     * @param attendanceFilePath Path to the attendance records CSV file
     * @return TreeMap with week start dates as keys and arrays of [workDuration, overtimeDuration] as values
     */
    private static TreeMap<LocalDate, Duration[]> processAttendanceData(String employeeId, String attendanceFilePath) {
        // Define standard work hours and break time
        LocalTime workStart = LocalTime.of(8, 0);                                   // Standard start time (8:00 AM)  
        LocalTime graceEnd = workStart.plusMinutes(10);                             // Grace period end (8:10 AM)
        LocalTime workEnd = LocalTime.of(17, 0);                                    // Standard end time (5:00 PM)
        Duration breakTime = Duration.ofHours(1);                                   // 1-hour break deduction per day                                   
        TreeMap<LocalDate, Duration[]> weeklyRecords = new TreeMap<>();             // Store weekly work and overtime durations

        try (CSVReader reader = new CSVReader(new FileReader(attendanceFilePath))) {
            reader.readNext(); // Skip the header row
            String[] attendanceRecord;          // Array to hold each attendance record
            while ((attendanceRecord = reader.readNext()) != null) {                // Read each row until end of file
                if (attendanceRecord.length >= 6 && attendanceRecord[0].trim().equals(employeeId)) {        // Match employee ID
                    processSingleAttendanceRecord(attendanceRecord, weeklyRecords, workStart, graceEnd, workEnd, breakTime);       // Process the record
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading attendance file: " + e.getMessage());         // Display error message
            e.printStackTrace();               // Print stack trace for debugging
        }
        return weeklyRecords;                  // Return the compiled weekly records
    }

    /**
     * Processes a single attendance record to calculate regular work and overtime durations
     * for a day, then updates the weekly records accordingly.
     *
     * @param attendanceRecord Array containing attendance data for a day
     * @param weeklyRecords TreeMap to store weekly durations
     * @param workStart Regular start time
     * @param graceEnd Grace period end time
     * @param workEnd Regular end time
     * @param breakTime Daily break duration to deduct
     */
    private static void processSingleAttendanceRecord(String[] attendanceRecord, TreeMap<LocalDate, Duration[]> weeklyRecords,
                                                      LocalTime workStart, LocalTime graceEnd, LocalTime workEnd, Duration breakTime) {
        String dateStr = attendanceRecord[3].trim();
        String logInStr = attendanceRecord[4].trim();
        String logOutStr = attendanceRecord[5].trim();

        if (logInStr.isEmpty() || logOutStr.isEmpty()) return;      // Skip records with missing times

        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);      // Parse the date
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) return;       // Skip weekends

            LocalTime logIn = LocalTime.parse(logInStr, TIME_FORMATTER);            // Parse login time
            LocalTime logOut = LocalTime.parse(logOutStr, TIME_FORMATTER);          // Parse logout time

            if (logOut.isBefore(logIn)) {
                System.out.println("Error: Invalid time record for " + attendanceRecord[0] + " on " + dateStr);     // Invalid time range
                return;
            }

            boolean isLate = logIn.isAfter(graceEnd);                                           // Check if employee is late beyond grace period
            if (logIn.isAfter(workStart) && logIn.isBefore(graceEnd)) logIn = workStart;        // Adjust login within grace period

            LocalTime adjustedLogOut = logOut.isAfter(workEnd) ? workEnd : logOut;              // Cap regular hours at workEnd
            Duration workDuration = Duration.between(logIn, adjustedLogOut).minus(breakTime);   // Calculate regular work hours minus break
            workDuration = workDuration.isNegative() ? Duration.ZERO : workDuration;            // Ensure no negative duration

            Duration overtimeDuration = (!isLate && logOut.isAfter(workEnd)) ? Duration.between(workEnd, logOut) : Duration.ZERO;      // Calculate overtime if not late

            LocalDate weekStart = date.with(DayOfWeek.MONDAY);                                  // Determine the Monday of the week
            weeklyRecords.putIfAbsent(weekStart, new Duration[]{Duration.ZERO, Duration.ZERO}); // Initialize week if not present
            Duration[] durations = weeklyRecords.get(weekStart);                                // Get existing durations
            durations[0] = durations[0].plus(workDuration);                                     // Add regular hours
            durations[1] = durations[1].plus(overtimeDuration);                                 // Add overtime hours
        } catch (DateTimeParseException e) {
            System.out.println("Error parsing attendance date/time for " + attendanceRecord[0] + " on " + dateStr);     // Parsing error
        }
    }

    /**
     * Calculates payroll components (base salary, overtime pay, deductions, allowances)
     * for each week and displays the summary to the user.
     *
     * @param fullName Employee's full name
     * @param weeklyRecords TreeMap of weekly work and overtime durations
     * @param hourlyRate Employee's hourly pay rate
     * @param riceSubsidy Monthly rice subsidy
     * @param phoneAllowance Monthly phone allowance
     * @param clothingAllowance Monthly clothing allowance
     */
    private static void calculateAndDisplayPayroll(String fullName, TreeMap<LocalDate, Duration[]> weeklyRecords,
                                                   double hourlyRate, double riceSubsidy, double phoneAllowance,
                                                   double clothingAllowance) {
        System.out.println("\nWeekly Salary Summary for " + fullName + ":");
        System.out.println("-------------------------------------------------");

        for (Map.Entry<LocalDate, Duration[]> entry : weeklyRecords.entrySet()) {           // Iterate through each week's records
            LocalDate startOfWeek = entry.getKey();                                         // Start of the week (Monday)
            LocalDate endOfWeek = startOfWeek.plusDays(4);                                  // End of the week (Friday, assuming 5-day work week)   
            Duration workDuration = entry.getValue()[0];                                    // Total regular work hours
            Duration overtimeDuration = entry.getValue()[1];                                // Total overtime hours

            // Calculate salary components
            double baseSalary = workDuration.toMinutes() / 60.0 * hourlyRate;                               // Convert minutes to hours and multiply by rate
            double overtimePay = overtimeDuration.toMinutes() / 60.0 * hourlyRate * OVERTIME_RATE;          // Overtime pay with multiplier
            double grossSalary = baseSalary + overtimePay;                                                  // Total salary before deductions
            double totalAllowances = (riceSubsidy + phoneAllowance + clothingAllowance) / WEEKLY_PAYMENTS;  // Weekly portion of monthly allowances

            // Calculate statutory deductions
            double sssContribution = calculateSSSContribution(grossSalary);                             // SSS contribution
            double philHealthContribution = calculatePhilHealthContribution(grossSalary);               // PhilHealth contribution
            double pagIbigContribution = calculatePagIbigContribution(grossSalary);                     // Pag-IBIG contribution
            double withholdingTax = calculateWithholdingTax(grossSalary);                               // Withholding tax
            double totalDeductions = sssContribution + philHealthContribution + pagIbigContribution;    // Sum of deductions

            double netSalary = grossSalary - totalDeductions - withholdingTax;                          // Salary after deductions and tax
            double finalPay = netSalary + totalAllowances;                                              // Final pay including allowances

            // Display the payroll details for the week
            printPayrollDetails(startOfWeek, endOfWeek, workDuration, overtimeDuration, baseSalary, overtimePay,
                    grossSalary, sssContribution, philHealthContribution, pagIbigContribution, totalDeductions,
                    withholdingTax, totalAllowances, finalPay);
        }
    }

    /**
     * Calculates the SSS (Social Security System) contribution based on the weekly gross salary.
     * Uses a predefined contribution table to determine the amount, which is then converted to a weekly value.
     *
     * @param grossSalary Weekly gross salary (base + overtime)
     * @return Weekly SSS contribution amount
     */
    private static double calculateSSSContribution(double grossSalary) {
        // Inner class to represent salary brackets and corresponding SSS contributions
        class SalaryContribution {
            double salaryLimit;     // Upper limit of the salary bracket (weekly)
            double contribution;    // Monthly SSS contribution for the bracket

            SalaryContribution(double salaryLimit, double contribution) {
                this.salaryLimit = salaryLimit;
                this.contribution = contribution;
            }
        }
        // Initialize SSS contribution table with monthly salary limits divided by 4 for weekly comparison
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

        //Initialize the SSS contribution to a default value of 0.0
        double sssContribution = 0.0;                                      
        for (SalaryContribution sssEntry : sssTable) {                      // Iterate through the SSS contribution table to find the appropriate contribution bracket
            if (grossSalary <= sssEntry.salaryLimit) {                      // Check if the employee's gross salary falls within the current bracket's salary limit
                 // If the salary fits within this bracket, calculate the weekly contribution
                 // by dividing the monthly contribution by the number of weekly payments (e.g., 4)
                sssContribution = sssEntry.contribution / WEEKLY_PAYMENTS;  
                break;  // Exit loop once contribution is found
            }
        }
        // If salary exceeds highest bracket, use the maximum contribution (converted to weekly)
        // Convert the maximum monthly contribution (1125.00) to a weekly value
        return (sssContribution == 0.0) ? 1125.00 / WEEKLY_PAYMENTS : sssContribution;
    }

    /**
     * Calculates the PhilHealth contribution based on the weekly gross salary.
     * Applies a tiered system: minimum contribution for low salaries, percentage-based
     * for mid-range, and a maximum cap for high salaries.
     *
     * @param grossSalary Weekly gross salary
     * @return Weekly PhilHealth contribution (employee's share)
     */
    private static double calculatePhilHealthContribution(double grossSalary) {
        double monthlyPremium = ((grossSalary * 0.03) / 2) / (4);                           // 3% of salary, split employer/employee, then monthly to weekly
        if (grossSalary <= 10000 / 4) return 300 / 2 / WEEKLY_PAYMENTS;                     // Minimum contribution for salaries up to 2500 weekly
        else if (grossSalary <= 59999.99 / 4) return monthlyPremium / 2 / WEEKLY_PAYMENTS;  // Calculated contribution up to ~15000 weekly
        else return 1800 / 2 / WEEKLY_PAYMENTS;                                             // Maximum contribution for higher salaries, weekly
    }

    /**
     * Calculates the Pag-ibig contribution based on the weekly gross salary.
     * Uses a tiered rate (1% or 2%) with a monthly cap of 100, converted to weekly.
     *
     * @param grossSalary Weekly gross salary
     * @return Weekly Pag-ibig contribution
     */
    private static double calculatePagIbigContribution(double grossSalary) {
        double contribution = (grossSalary <= 1500) ? grossSalary * 0.01 : grossSalary * 0.02;  // 1% if <= 1500, else 2%
        return Math.min(contribution, 100.00 / WEEKLY_PAYMENTS);                                // Cap at 100 monthly, converted to weekly
    }

    /**
     * Calculates the withholding tax based on the weekly gross salary using tax brackets.
     * Note: This implementation may not align with the latest tax tables; consider updating
     * based on current regulations.
     *
     * @param grossSalary Weekly gross salary
     * @return Weekly withholding tax amount
     */
    private static double calculateWithholdingTax(double grossSalary) {
        if (grossSalary <= 20832 / 4) return 0.0;                                                       // No tax for weekly salary <= 5208
        else if (grossSalary <= 33333 / 4) return ((grossSalary - 20833 / 4) * 0.20);                   // 20% over 5208 up to 8332.5
        else if (grossSalary <= 66667 / 4) return (2500 / 4) + ((grossSalary - 33333 / 4) * 0.25);      // 625 + 25% over 8332.5 up to 16666.75
        else if (grossSalary <= 166667 / 4) return (10833 / 4) + ((grossSalary - 66667 / 4) * 0.30);    // 2708.25 + 30% over 16666.75 up to 41666.75
        else if (grossSalary <= 666667) return (40833.33 / 4) + ((grossSalary - 166667 / 4) * 0.32);    // 10208.33 + 32% over 41666.75 up to 166666.75
        else return (208333.33 / 4) + ((grossSalary - 166667) * 0.35);                                  // 52083.33 + 35% over 166666.75
    }

    /**
     * Prints a detailed payroll summary for a specific week, including hours worked,
     * salary components, deductions, allowances, and net pay.
     *
     * @param startOfWeek Start date of the week
     * @param endOfWeek End date of the week
     * @param workDuration Total regular work hours for the week
     * @param overtimeDuration Total overtime hours for the week
     * @param baseSalary Base pay for regular hours
     * @param overtimePay Additional pay for overtime hours
     * @param grossSalary Total salary before deductions
     * @param sssContribution SSS deduction
     * @param philHealthContribution PhilHealth deduction
     * @param pagIbigContribution Pag-IBIG deduction
     * @param totalDeductions Sum of all statutory deductions
     * @param withholdingTax Tax withheld
     * @param totalAllowances Weekly allowances
     * @param finalPay Net pay after all calculations
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
