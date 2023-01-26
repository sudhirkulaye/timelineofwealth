package com.timelineofwealth.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

public class TodoListPDF {
    public static void main(String[] args) throws FileNotFoundException, DocumentException {
        // Create a new PDF document
        Document document = new Document();
        // Create a new PDF file for each day
        String filename = String.format("Planner_%02d.pdf", new Date().getTime());
        PdfWriter.getInstance(document, new FileOutputStream(new File("C:\\MyDocuments\\03Business\\" + filename)));
        document.open();

        // Get the current year
        int year = Year.now().getValue();
        // Check if the current year is 2023. If not, set the year to 2023
        if (year != 2023) {
            year = 2023;
        }

        Map<LocalDate, List<String>> dailyTodoMap = getDailyToDoMap(year);
        Map<LocalDate, List<String>> weekendToDoMap = getWeekendToDoMap(year);
        Map<LocalDate, List<String>> monthlyTodoMap = getMonthlyToDoMap(year);
        Map<LocalDate, List<String>> quarterlyTodoMap = getQyarterlyToDoMap(year);
        Map<LocalDate, List<String>> fourMonthToDoMap = getFourMonthToDoMap(year);
        Map<LocalDate, List<String>> semiAnnualToDoMap = getSemiAnnualToDoMap(year);
        Map<LocalDate, List<String>> annualToDoMap = getAnnualToDoMap(year);

        // Create a LocalDate object for January 1st of the current year
        LocalDate date = LocalDate.of(year, 1, 1);

        // Loop through each day of the year
        while (date.getYear() == year) {

            // Get the list of activites for the date
            List dailyTodoList = dailyTodoMap.get(date);
            List weekendToDoList = weekendToDoMap.get(date);
            List monthlyTodoList = monthlyTodoMap.get(date);
            List quarterlyTodoList = quarterlyTodoMap.get(date);
            List fourMonthToDoList = fourMonthToDoMap.get(date);
            List semiAnnualToDoList = semiAnnualToDoMap.get(date);
            List annualToDoList = annualToDoMap.get(date);


            dailyTodoList.addAll(weekendToDoList);
            dailyTodoList.addAll(monthlyTodoList);
            if (quarterlyTodoList != null)
                dailyTodoList.addAll(quarterlyTodoList);
            if (fourMonthToDoList != null)
                dailyTodoList.addAll(fourMonthToDoList);
            if (semiAnnualToDoList != null)
                dailyTodoList.addAll(semiAnnualToDoList);
            if (annualToDoList != null)
                dailyTodoList.addAll(annualToDoList);

            if (dailyTodoList.size() > 30)
                System.out.println("Date - dailyTodoList.size() " + date.getYear() + " - " + date.getMonth() + "-" + date.getDayOfMonth() + " --- " + dailyTodoList.size());

            // Add the date to the document
            document.add(new Paragraph(date.toString()));

            // Add the day of the week to the document
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            document.add(new Paragraph(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)));

            // Add a blank line to the document
            document.add(new Paragraph(" "));

            // Create a table for the hourly planner
            PdfPTable table = new PdfPTable(2);
            table.setWidths(new int[]{1, 1});

            // Add the Activity list to the left column of the table
            table.addCell("Activities:");
            table.addCell("Schedule");

            // Create a LocalTime object for 5:00 AM
            LocalTime time = LocalTime.of(4, 0);

            // Add the hours to the right column of the table & task list  to the left column
            int taskcount = 0;
            int dailyTodoListSize = dailyTodoList.size();
            while (time.getHour() < 23) {
                String task = " ";
                if (taskcount < dailyTodoListSize) {
                    task = (String) dailyTodoList.get(taskcount);
                }
                table.addCell(task);
                table.addCell(time.toString());
                time = time.plusMinutes(30);
                taskcount++;
            }

            // Add the table to the document
            document.add(table);

            // Add notes
            document.add(new Paragraph("Notes of the Day: "));

            // Increment the date by one day
            date = date.plusDays(1);

            document.newPage();
        }
        // Close the document
        document.close();
    }

    public static Map<LocalDate, List<String>> getDailyToDoMap(int forYear) {
        // Create a map for daily activities
        Map<LocalDate, List<String>> dailyTodoMap = new HashMap<>();

        // Get the current year
        int year = Year.now().getValue();
        // Check if the current year is 2023. If not, set the year to 2023
        if (year != forYear) {
            year = forYear;
        }

        // Create a LocalDate object for January 1st of the current year
        LocalDate date = LocalDate.of(year, 1, 1);

        // Loop through each day of the year
        while (date.getYear() == year) {
            // Create a new list for the daily activities
            List<String> todos = new ArrayList<>();

            // Add the "Daily Activities" activity on weekdays (Monday to Friday)
            if (date.getDayOfWeek().getValue() >= DayOfWeek.MONDAY.getValue() && date.getDayOfWeek().getValue() <= DayOfWeek.FRIDAY.getValue()) {
                todos.add("Daily Activities:");
                todos.add("  [  ] Meditation");
                todos.add("  [  ] Morning Reading");
                todos.add("  [  ] Valuation Review");
                todos.add("  [  ] Morning Hyper-Focus");
                todos.add("  [  ] Gym");
                todos.add("  [  ] Trade Execution & Verification");
                todos.add("  [  ] Noon Hyper-Focus");
                todos.add("  [  ] Walk");
                todos.add("  [  ] News Archive");
                todos.add("  [  ] MOSL Daily Reports");
                todos.add("  [  ] Ambit Daily Reports");
                todos.add("  [  ] Kotak Daily Reports");
                todos.add("  [  ] Other Reports");
                todos.add("  [  ] Download Daily Data");
                todos.add("  [  ] EOD Local");
                todos.add("  [  ] Result Tracker Update");
                todos.add("  [  ] Audible Book");
                todos.add("  [  ] Night Fiction Reading");
            }

            // Put the date and activities in the map
            dailyTodoMap.put(date, todos);

            // Increment the date by one day
            date = date.plusDays(1);
        }

        return dailyTodoMap;
    }

    public static Map<LocalDate, List<String>> getWeekendToDoMap(int forYear) {
        // Create a map for weekend activities
        Map<LocalDate, List<String>> weekendTodoMap = new HashMap<>();

        // Get the current year
        int year = Year.now().getValue();
        // Check if the current year is 2023. If not, set the year to 2023
        if (year != forYear) {
            year = forYear;
        }

        // Create a LocalDate object for January 1st of the current year
        LocalDate date = LocalDate.of(year, 1, 1);

        // Loop through each day of the year
        while (date.getYear() == year) {
            // Create a new list for the weekend activities
            List<String> todos = new ArrayList<>();

            // Add the "Weekend Review" activity for every Sunday
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                todos.add("Daily Activities:");
                todos.add("  [  ] Meditation");
                todos.add("  [  ] Morning Reading");
                todos.add("  [  ] Valuation Review");
                todos.add("  [  ] Morning Hyper-Focus");
                todos.add("  [  ] Result Tracker Update");
                todos.add("  [  ] Walk");
                todos.add("  [  ] Audible Book");
                todos.add("  [  ] Night Fiction Reading");
                todos.add("Weekend Activities:");
                todos.add("  [  ] Yoga");
                todos.add("  [  ] Weekly Review");
            }

            // Add the "Weekend Review" activity for every Sunday
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                todos.add("Daily Activities:");
                todos.add("  [  ] Meditation");
                todos.add("  [  ] Morning Reading");
                todos.add("  [  ] Stock Reviews");
                todos.add("  [  ] Morning Hyper-Focus");
                todos.add("  [  ] Download Daily Data");
                todos.add("  [  ] EOD Local");
                todos.add("  [  ] Result Tracker Update");
                todos.add("  [  ] Walk");
                todos.add("  [  ] News Archive");
                todos.add("  [  ] Audible Book");
                todos.add("  [  ] Night Fiction Reading");
            }

            // Put the date and activities in the map
            weekendTodoMap.put(date, todos);

            // Increment the date by one day
            date = date.plusDays(1);
        }

        return weekendTodoMap;
    }

    public static Map<LocalDate, List<String>> getMonthlyToDoMap(int forYear) {
        // Create a map for daily activities
        Map<LocalDate, List<String>> monthlyTodoMap = new HashMap<>();

        // Get the current year
        int year = Year.now().getValue();
        // Check if the current year is 2023. If not, set the year to 2023
        if (year != forYear) {
            year = forYear;
        }

        // Create a LocalDate object for January 1st of the current year
        LocalDate date = LocalDate.of(year, 1, 1);

        // Loop through each day of the year
        while (date.getYear() == year) {
            // Create a new list for the daily activities
            List<String> todos = new ArrayList<>();

            // Add the "Month End Activities" activity on the first day of the month
            if (date.getDayOfMonth() == 1) {
                todos.add("Monthly Activities:");
                todos.add("  [  ] Index Data Update");
                todos.add("  [  ] Auditing Bank Balance");
                todos.add("  [  ] Auditing FDs");
                todos.add("  [  ] Auditing US Market Portfolio");
                todos.add("  [  ] Pay Credit Card Bills");
                todos.add("  [  ] Pay MSEB/BEST/MGL Bill");
                todos.add("  [  ] Pay Staff Salary");
                todos.add("  [  ] Auditing Monthly Expenses");
                todos.add("  [  ] Auditing Wealth");
                todos.add("  [  ] Asset Allocation");
                todos.add("  [  ] MOSL Report Download");
                todos.add("  [  ] TOW Report Generation");
                todos.add("  [  ] EOM Report Communication");
                todos.add("  [  ] Matching Stock Count");
                todos.add("  [  ] Database Export & Import");
            }

            // Add the "Month End Activities" activity on the first day of the month
            if (date.getDayOfMonth() == 17) {
                todos.add("Monthly Activities:");
                todos.add("  [  ] Pay MTNL Bill");
            }

            // Add the "Month End Activities" activity on the first day of the month
            if (date.getDayOfMonth() == 3) {
                todos.add("Monthly Activities:");
                todos.add("  [  ] Auditing Auto Sales");
                todos.add("  [  ] Pay AWS Bill");
            }

            // Put the date and activities in the map
            monthlyTodoMap.put(date, todos);

            // Increment the date by one day
            date = date.plusDays(1);
        }

        return monthlyTodoMap;
    }

    public static Map<LocalDate, List<String>> getBimonthlyToDoMap(int forYear) {

        // Create a map for daily activities
        Map<LocalDate, List<String>> bimonthlyTodoMap = new HashMap<>();

        return bimonthlyTodoMap;

    }

    public static Map<LocalDate, List<String>> getQyarterlyToDoMap(int forYear) {

        // Create a map for daily activities
        Map<LocalDate, List<String>> quarterlyTodoMap = new HashMap<>();

        // Get the current year
        int year = Year.now().getValue();
        // Check if the current year is 2023. If not, set the year to 2023
        if (year != forYear) {
            year = forYear;
        }

        // Pay Diamond Park Maintenance on Every 14th Date of March, Jun, Sep & Dec
        LocalDate date = LocalDate.of(year, 3, 14);
        List<String> todos = new ArrayList<>();
        todos.add("Quarterly Activities:");
        todos.add("  [  ] Pay Diamond Park Maintenance");
        quarterlyTodoMap.put(date, todos);

        date = LocalDate.of(year, 6, 14);
        todos = new ArrayList<>();
        todos.add("Quarterly Activities:");
        todos.add("  [  ] Pay Diamond Park Maintenance");
        quarterlyTodoMap.put(date, todos);

        date = LocalDate.of(year, 9, 14);
        todos = new ArrayList<>();
        todos.add("Quarterly Activities:");
        todos.add("  [  ] Pay Diamond Park Maintenance");
        quarterlyTodoMap.put(date, todos);

        date = LocalDate.of(year, 12, 14);
        todos = new ArrayList<>();
        todos.add("Quarterly Activities:");
        todos.add("  [  ] CGHS Visit");
        quarterlyTodoMap.put(date, todos);

        // Pay Diamond Park Maintenance on Every 14th Date of March, Jun, Sep & Dec
        date = LocalDate.of(year, 2, 8);
        todos = new ArrayList<>();
        todos.add("Quarterly Activities:");
        todos.add("  [  ] CGHS Visit");
        quarterlyTodoMap.put(date, todos);

        date = LocalDate.of(year, 5, 8);
        todos = new ArrayList<>();
        todos.add("Quarterly Activities:");
        todos.add("  [  ] CGHS Visit");
        quarterlyTodoMap.put(date, todos);

        date = LocalDate.of(year, 8, 8);
        todos = new ArrayList<>();
        todos.add("Quarterly Activities:");
        todos.add("  [  ] CGHS Visit");
        quarterlyTodoMap.put(date, todos);

        date = LocalDate.of(year, 11, 8);
        todos = new ArrayList<>();
        todos.add("Quarterly Activities:");
        todos.add("  [  ] CGHS Visit");
        quarterlyTodoMap.put(date, todos);

        // Add Quarterly Activities of 1. Adding IPO 2. Stock Split & Bonus
        date = LocalDate.of(year, 1, 1);
        todos = new ArrayList<>();
        boolean isActivityAdded = false;
        int count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
                count++;
                if (count == 1) {
                    todos.add("Quarterly Activities:");
                    todos.add("  [  ] Adding IPOs");
                    quarterlyTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }
        date = LocalDate.of(year, 1, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                count++;
                if (count == 1) {
                    todos.add("Quarterly Activities:");
                    todos.add("  [  ] Updating Splits & Bonus");
                    quarterlyTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }
        date = LocalDate.of(year, 4, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
                count++;
                if (count == 1) {
                    todos.add("Quarterly Activities:");
                    todos.add("  [  ] Adding IPOs");
                    quarterlyTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }
        date = LocalDate.of(year, 4, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                count++;
                if (count == 1) {
                    todos.add("Quarterly Activities:");
                    todos.add("  [  ] Updating Splits & Bonus");
                    quarterlyTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }
        date = LocalDate.of(year, 7, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
                count++;
                if (count == 1) {
                    todos.add("Quarterly Activities:");
                    todos.add("  [  ] Adding IPOs");
                    quarterlyTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }
        date = LocalDate.of(year, 7, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                count++;
                if (count == 1) {
                    todos.add("Quarterly Activities:");
                    todos.add("  [  ] Updating Splits & Bonus");
                    quarterlyTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }
        date = LocalDate.of(year, 10, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
                count++;
                if (count == 1) {
                    todos.add("Quarterly Activities:");
                    todos.add("  [  ] Adding IPOs");
                    quarterlyTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }
        date = LocalDate.of(year, 10, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                count++;
                if (count == 1) {
                    todos.add("Quarterly Activities:");
                    todos.add("  [  ] Updating Splits & Bonus");
                    quarterlyTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }

        return quarterlyTodoMap;

    }

    public static Map<LocalDate, List<String>> getFourMonthToDoMap(int forYear) {

        // Create a map for daily activities
        Map<LocalDate, List<String>> fourMonthTodoMap = new HashMap<>();

        // Get the current year
        int year = Year.now().getValue();
        // Check if the current year is 2023. If not, set the year to 2023
        if (year != forYear) {
            year = forYear;
        }

        // Add activity to refill water in the inverter after four months
        LocalDate date = LocalDate.of(year, 1, 31);
        List<String> todos = new ArrayList<>();
        todos.add("Four Month Activities:");
        todos.add("  [  ] Inverter Battery Water");
        fourMonthTodoMap.put(date, todos);

        date = LocalDate.of(year, 5, 31);
        todos = new ArrayList<>();
        todos.add("Four Month Activities:");
        todos.add("  [  ] Inverter Battery Water");
        fourMonthTodoMap.put(date, todos);

        date = LocalDate.of(year, 9, 30);
        todos = new ArrayList<>();
        todos.add("Four Month Activities:");
        todos.add("  [  ] Inverter Battery Water");
        fourMonthTodoMap.put(date, todos);


        return fourMonthTodoMap;

    }

    public static Map<LocalDate, List<String>> getSemiAnnualToDoMap(int forYear) {

        // Create a map for daily activities
        Map<LocalDate, List<String>> semiAnnualTodoMap = new HashMap<>();

        // Get the current year
        int year = Year.now().getValue();
        // Check if the current year is 2023. If not, set the year to 2023
        if (year != forYear) {
            year = forYear;
        }

        // Add Quarterly Activities of 1. Index Constituents 2. Screener List
        LocalDate date = LocalDate.of(year, 4, 1);
        List<String> todos = new ArrayList<>();
        boolean isActivityAdded = false;
        int count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                count++;
                if (count == 1) {
                    todos.add("Semi Annual Activities:");
                    todos.add("  [  ] Update Index Constituents");
                    todos.add("  [  ] Update Screener List");
                    semiAnnualTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }
        date = LocalDate.of(year, 10, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                count++;
                if (count == 1) {
                    todos.add("Semi Annual Activities:");
                    todos.add("  [  ] Update Index Constituents");
                    todos.add("  [  ] Update Screener List");
                    semiAnnualTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }

        return semiAnnualTodoMap;

    }

    public static Map<LocalDate, List<String>> getAnnualToDoMap(int forYear) {

        // Create a map for daily activities
        Map<LocalDate, List<String>> annualTodoMap = new HashMap<>();

        // Get the current year
        int year = Year.now().getValue();
        // Check if the current year is 2023. If not, set the year to 2023
        if (year != forYear) {
            year = forYear;
        }

        //********************************January Activities********************************//
        // Add Annual Rent Collection Activity for Safalya Room on every 2nd Saturday of January
        LocalDate date = LocalDate.of(year, 1, 1);
        List<String> todos = new ArrayList<>();
        boolean isActivityAdded = false;
        int count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                count++;
                if (count == 2) {
                    todos.add("Annual Activities:");
                    todos.add("  [  ] Safalya Rent Collection");
                    annualTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }

        // Add Annual Activity to Download Annual Statement of Banks and Tax Saving Investments on every 2nd Sunday of January
        date = LocalDate.of(year, 1, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                count++;
                if (count == 2) {
                    todos.add("Annual Activities:");
                    todos.add("  [  ] Download Bank Statements");
                    todos.add("  [  ] Tax Saving Investments");
                    annualTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }

        //********************************March Activities********************************//
        // Add Annual AC Servicing Activity for Safalya Room on every 4th Sunday of March
        date = LocalDate.of(year, 3, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                count++;
                if (count == 4) {
                    todos.add("Annual Activities:");
                    todos.add("  [  ] AC Servicing");
                    annualTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }

        //********************************April Activities********************************//
        // Add Annual Rent Payment To MHADA on every 1st April
        date = LocalDate.of(year, 4, 1);
        todos = new ArrayList<>();
        todos.add("Annual Activities:");
        todos.add("  [  ] MHADA Rent Payment");
        annualTodoMap.put(date, todos);

        //********************************Jun Activities********************************//
        // Add Annual Tax Filing Activity for Self & Aai on every 4th Sunday of Jun & Pay Car Insurance
        date = LocalDate.of(year, 6, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                count++;
                if (count == 4) {
                    todos.add("Annual Activities:");
                    todos.add("  [  ] Pay Car Insurance");
                    todos.add("  [  ] Tax Filing For Self & Aai");
                    annualTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }

        //********************************July Activities********************************//
        // Add Annual Tax Filing Activity for Rohan's Aai on every 1st Sunday of Jul
        date = LocalDate.of(year, 7, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                count++;
                if (count == 1) {
                    todos.add("Annual Activities:");
                    todos.add("  [  ] Tax Filing For Rohan Aai");
                    annualTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }

        // Add Annual Tax Filing Activity for Jayadada on every 2nd Sunday of Jul
        date = LocalDate.of(year, 7, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                count++;
                if (count == 2) {
                    todos.add("Annual Activities:");
                    todos.add("  [  ] Tax Filing For Jayadada");
                    annualTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }

        // Add Annual Tax Filing Activity for Rohini on every 3rd Sunday of Jul
        date = LocalDate.of(year, 7, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                count++;
                if (count == 3) {
                    todos.add("Annual Activities:");
                    todos.add("  [  ] Tax Filing For Rohini");
                    annualTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }

        // Add Annual Tax Filing Activity for Madhavi on every 4th Sunday of Jul
        date = LocalDate.of(year, 7, 1);
        todos = new ArrayList<>();
        isActivityAdded = false;
        count = 0;
        while (!isActivityAdded) {
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                count++;
                if (count == 4) {
                    todos.add("Annual Activities:");
                    todos.add("  [  ] Tax Filing For Madhavi");
                    annualTodoMap.put(date, todos);
                    isActivityAdded = true;
                }
            }
            date = date.plusDays(1);
        }

        //********************************September Activities********************************//
        // Add Annual Gharpatti Payment to Grampanchayat on 1st Sep
        date = LocalDate.of(year, 9, 1);
        todos = new ArrayList<>();
        todos.add("Annual Activities:");
        todos.add("  [  ] Pay Village Gharpatti");
        annualTodoMap.put(date, todos);

        //********************************November Activities********************************//
        // Add Annual Get Life Certificate on 1st Nov
        date = LocalDate.of(year, 11, 1);
        todos = new ArrayList<>();
        todos.add("Annual Activities:");
        todos.add("  [  ] Get Life Certificate");
        annualTodoMap.put(date, todos);

        //********************************December Activities********************************//
        // Add Annual Get Life Certificate on 1st Nov
        date = LocalDate.of(year, 12, 1);
        todos = new ArrayList<>();
        todos.add("Annual Activities:");
        todos.add("  [  ] Pay Diamond Park Prop. Tax");
        annualTodoMap.put(date, todos);

        return annualTodoMap;

    }


}
