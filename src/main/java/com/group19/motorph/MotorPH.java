/*
 * Click nbfs://SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.group19.motorph;

import com.opencsv.CSVReader;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class MotorPH {
    public static void main(String[] args) {
        String employeeFilePath = "src/main/employeedata.csv";
        String attendanceFilePath = "src/main/attendancerecord.csv";

        Scanner inputScanner = new Scanner(System.in);
        System.out.println("Welcome to MotorPH Payroll!");
        System.out.println("[1] Compute Payroll for a Specific Employee");
        System.out.println("[2] Compute Payroll for All Employees");
        System.out.print("Enter selection: ");
        int choice = inputScanner.nextInt();
        inputScanner.nextLine(); // Consume newline

        if (choice == 1) {
            System.out.print("\nEnter Employee Number: ");
            String searchEmployeeNumber = inputScanner.nextLine().trim();

            if (searchEmployeeNumber.isEmpty()) {
                System.out.println("No input provided. Exiting program.");
                return;
            }
            processPayrollForEmployee(searchEmployeeNumber, employeeFilePath, attendanceFilePath);
        } else if (choice == 2) {
            processPayrollForAllEmployees(employeeFilePath, attendanceFilePath);
        } else {
            System.out.println("Invalid choice. Exiting program.");
        }
    }

    private static void processPayrollForEmployee(String employeeNumber, String employeeFilePath, String attendanceFilePath) {
        try (CSVReader reader = new CSVReader(new FileReader(employeeFilePath))) {
            reader.readNext(); // Skip header
            String[] data;
            while ((data = reader.readNext()) != null) {
                if (data.length >= 19 && data[0].trim().equals(employeeNumber)) {
                    String fullName = data[1].trim() + ", " + data[2].trim();
                    String birthDay = data[3].trim();
                    double hourlyRate = Double.parseDouble(data[18].trim().replaceAll("\"", "").replace(",", ""));
                    double riceSubsidy = Double.parseDouble(data[14].trim().replaceAll("\"", "").replace(",", ""));
                    double phoneAllowance = Double.parseDouble(data[15].trim().replaceAll("\"", "").replace(",", ""));
                    double clothingAllowance = Double.parseDouble(data[16].trim().replaceAll("\"", "").replace(",", ""));
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
            e.printStackTrace(); // Add stack trace for debugging
        }
    }

    private static void processPayrollForAllEmployees(String employeeFilePath, String attendanceFilePath) {
        try (CSVReader reader = new CSVReader(new FileReader(employeeFilePath))) {
            reader.readNext(); // Skip header
            String[] data;
            while ((data = reader.readNext()) != null) {
                if (data.length >= 19) {
                    String employeeNumber = data[0].trim();
                    String fullName = data[1].trim() + ", " + data[2].trim();
                    String birthDay = data[3].trim();
                    double hourlyRate = Double.parseDouble(data[18].trim().replaceAll("\"", "").replace(",", ""));
                    double riceSubsidy = Double.parseDouble(data[14].trim().replaceAll("\"", "").replace(",", ""));
                    double phoneAllowance = Double.parseDouble(data[15].trim().replaceAll("\"", "").replace(",", ""));
                    double clothingAllowance = Double.parseDouble(data[16].trim().replaceAll("\"", "").replace(",", ""));
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
            e.printStackTrace(); // Add stack trace for debugging
        }
    }

    private static void calculateWeeklyWorkHours(String employeeId, String fullName, double hourlyRate, 
                                                 double riceSubsidy, double phoneAllowance, double clothingAllowance, 
                                                 String attendanceFilePath) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime graceEnd = workStart.plusMinutes(10);
        LocalTime workEnd = LocalTime.of(17, 0);
        Duration breakTime = Duration.ofHours(1);

        TreeMap<LocalDate, Duration[]> weeklyRecords = new TreeMap<>();

        try (CSVReader reader = new CSVReader(new FileReader(attendanceFilePath))) {
            reader.readNext(); // Skip header

            String[] data;
            while ((data = reader.readNext()) != null) {
                if (data.length >= 6) {
                    String empId = data[0].trim();
                    if (!empId.equals(employeeId)) continue;

                    String dateStr = data[3].trim();
                    String logInStr = data[4].trim();
                    String logOutStr = data[5].trim();

                    if (logInStr.isEmpty() || logOutStr.isEmpty()) continue;

                    try {
                        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
                        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;

                        LocalTime logIn = LocalTime.parse(logInStr, timeFormatter);
                        LocalTime logOut = LocalTime.parse(logOutStr, timeFormatter);

                        if (logOut.isBefore(logIn)) {
                            System.out.println("Error: Invalid time record for " + empId + " on " + dateStr);
                            continue;
                        }

                        boolean isLate = logIn.isAfter(graceEnd);
                        if (logIn.isAfter(workStart) && logIn.isBefore(graceEnd)) {
                            logIn = workStart;
                        }

                        LocalTime adjustedLogOut = logOut.isAfter(workEnd) ? workEnd : logOut;
                        Duration workDuration = Duration.between(logIn, adjustedLogOut).minus(breakTime);
                        workDuration = workDuration.isNegative() ? Duration.ZERO : workDuration;

                        Duration overtimeDuration = (!isLate && logOut.isAfter(workEnd))
                                ? Duration.between(workEnd, logOut)
                                : Duration.ZERO;

                        LocalDate weekStart = date.with(DayOfWeek.MONDAY);
                        weeklyRecords.putIfAbsent(weekStart, new Duration[]{Duration.ZERO, Duration.ZERO});
                        Duration[] durations = weeklyRecords.get(weekStart);
                        durations[0] = durations[0].plus(workDuration);
                        durations[1] = durations[1].plus(overtimeDuration);
                    } catch (DateTimeParseException e) {
                        System.out.println("Error parsing attendance date/time for " + empId + " on " + dateStr);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
            e.printStackTrace(); // Add stack trace for debugging
            return;
        }

        System.out.println("\nWeekly Salary Summary for " + fullName + ":");
        System.out.println("-------------------------------------------------");
        for (Map.Entry<LocalDate, Duration[]> entry : weeklyRecords.entrySet()) {
            LocalDate startOfWeek = entry.getKey();
            LocalDate endOfWeek = startOfWeek.plusDays(4);
            Duration workDuration = entry.getValue()[0];
            Duration overtimeDuration = entry.getValue()[1];

            double baseSalary = (workDuration.toMinutes() / 60.0 * hourlyRate);
            double overtimePay = (overtimeDuration.toMinutes() / 60.0 * hourlyRate * 0.25);
            double grossSalary = baseSalary + overtimePay;
            double totalAllowances = (riceSubsidy + phoneAllowance + clothingAllowance) / 4.0;

            // Deduction calculations
            double sssContribution = 0.0;
            if (grossSalary < 3250) {
                sssContribution = 135.00 / 4;
            } else if (grossSalary <= 3750) {
                sssContribution = 157.50 / 4;
            } else if (grossSalary <= 4250) {
                sssContribution = 180.00 / 4;
            } else if (grossSalary <= 4750) {
                sssContribution = 202.50 / 4;
            } else if (grossSalary <= 5250) {
                sssContribution = 225.00 / 4;
            } else if (grossSalary <= 5750) {
                sssContribution = 247.50 / 4;
            } else if (grossSalary <= 6250) {
                sssContribution = 270.00 / 4;
            } else if (grossSalary <= 6750) {
                sssContribution = 292.50 / 4;
            } else if (grossSalary <= 7250) {
                sssContribution = 315.00 / 4;
            } else if (grossSalary <= 7750) {
                sssContribution = 337.50 / 4;
            } else if (grossSalary <= 8250) {
                sssContribution = 360.00 / 4;
            } else if (grossSalary <= 8750) {
                sssContribution = 382.50 / 4;
            } else if (grossSalary <= 9250) {
                sssContribution = 405.00 / 4;
            } else if (grossSalary <= 9750) {
                sssContribution = 427.50 / 4;
            } else if (grossSalary <= 10250) {
                sssContribution = 450.00 / 4;
            } else if (grossSalary <= 10750) {
                sssContribution = 472.50 / 4;
            } else if (grossSalary <= 11250) {
                sssContribution = 495.00 / 4;
            } else if (grossSalary <= 11750) {
                sssContribution = 517.50 / 4;
            } else if (grossSalary <= 12250) {
                sssContribution = 540.00 / 4;
            } else if (grossSalary <= 12750) {
                sssContribution = 562.50 / 4;
            } else if (grossSalary <= 13250) {
                sssContribution = 585.00 / 4;
            } else if (grossSalary <= 13750) {
                sssContribution = 607.50 / 4;
            } else if (grossSalary <= 14250) {
                sssContribution = 630.00 / 4;
            } else if (grossSalary <= 14750) {
                sssContribution = 652.50 / 4;
            } else if (grossSalary <= 15250) {
                sssContribution = 675.00 / 4;
            } else {
                sssContribution = 697.50 / 4; // Max SSS Contribution
            }

            double philHealthContribution = 0.0;
            double monthlyPremium = grossSalary * 0.03; // 3% of Monthly Basic Salary
            if (grossSalary <= 10000) {
                philHealthContribution = 300 / 2; // Employee's share (50%)
            } else if (grossSalary <= 59999.99) {
                philHealthContribution = (monthlyPremium / 2); // Employee's share (50%)
            } else {
                philHealthContribution = 1800 / 2; // Max PhilHealth contribution (50%)
            }

            double pagIbigContribution = 0.0;
            if (grossSalary <= 1500) {
                pagIbigContribution = grossSalary * 0.01; // Employee's share (1%)
            } else {
                pagIbigContribution = grossSalary * 0.02; // Employee's share (2%)
            }
            if (pagIbigContribution > 100) {
                pagIbigContribution = 100.00 / 4;
            }

            double withholdingTax = 0.0;
            if (grossSalary <= 5208) {
                withholdingTax = 0.0;
            } else if (grossSalary <= 8333) {
                withholdingTax = (grossSalary - 5208) * 0.20;
            } else if (grossSalary <= 16667) {
                withholdingTax = 625 + (grossSalary - 8333) * 0.25;
            } else if (grossSalary <= 41667) {
                withholdingTax = 2708.25 + (grossSalary - 16667) * 0.30;
            } else if (grossSalary <= 166667) {
                withholdingTax = 10208.33 + (grossSalary - 41667) * 0.32;
            } else {
                withholdingTax = 50208.33 + (grossSalary - 166667) * 0.35;
            }

            double totalDeduction = sssContribution + philHealthContribution + pagIbigContribution + withholdingTax;
            double netSalary = grossSalary - withholdingTax - sssContribution - philHealthContribution - pagIbigContribution;
            double finalPay = netSalary + totalAllowances;

            // Updated output with allowances
            System.out.println("Week Period             : " + startOfWeek + " - " + endOfWeek);
            System.out.println("Total Hours Worked      : " + workDuration.toHours() + "h " + workDuration.toMinutesPart() + "m");
            System.out.println("Total Overtime          : " + overtimeDuration.toHours() + "h " + overtimeDuration.toMinutesPart() + "m");
            System.out.println("Base Salary             : PHP " + String.format("%.2f", baseSalary));
            System.out.println("Overtime Pay            : PHP " + String.format("%.2f", overtimePay));
            System.out.println("\nGross Salary            : PHP " + String.format("%.2f", grossSalary));
            System.out.println("SSS Contribution        : PHP " + String.format("-%.2f", sssContribution));
            System.out.println("PhilHealth Contribution : PHP " + String.format("-%.2f", philHealthContribution));
            System.out.println("Pag-Ibig Contribution   : PHP " + String.format("-%.2f", pagIbigContribution));
            System.out.println("Withholding Tax         : PHP " + String.format("-%.2f", withholdingTax));
            System.out.println("\nTotal Deductions        : PHP " + String.format("-%.2f",  totalDeduction));
            System.out.println("\nAllowances              : PHP " + String.format("%.2f", totalAllowances));
            System.out.println("\nNet Salary              : PHP " + String.format("%.2f", finalPay));
            System.out.println("--------------------------------------------------");
        }
    }
}