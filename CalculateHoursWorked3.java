
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


public class CalculateHoursWorked3 {
    public static void main(String[] args) {
    	
    	//Declaring the file path
    	String employeeFile = "C:\\Users\\Huawei\\Documents\\MMDC\\MotorPH Files\\Employee Details.csv";
    	String attendanceFile = "C:\\Users\\Huawei\\Documents\\MMDC\\MotorPH Files\\Attendance Record.csv";
    	
    	
    	//Asking user to enter employee information that will be filtered out in the CSV file
    	Scanner sc = new Scanner(System.in);
    	System.out.print("Enter Employee Number: ");
    	String input = sc.nextLine();
    	
    	sc.close();
    	//Placeholder for employee information
    	String employeeNumber = "";
    	String firstName = "";
    	String lastName = "";
    	String employeeBirthday = "";
    	double hourlyRate = 0;
    	boolean found = false;
    	
    	//Read CSV File and returns data to each placeholder
    	try (BufferedReader read = new BufferedReader(new FileReader(employeeFile))){
    		read.readLine();
    		String line;
    	    while ((line = read.readLine()) != null) {
    	        if (line.trim().isEmpty()) continue;
    	        String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    	        	
    	        	if (data[0].equals(input)) {
	    	            employeeNumber = data[0];
	    	            firstName = data[1];
	    	            lastName = data[2];
	    	            employeeBirthday = data[3];
	    	            hourlyRate = Double.parseDouble(data[18]); //converts the string into double as this is an amount
	    	            found = true;
	    	            break;
    	        	}
    	    }
    	}
    	catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
    	catch (IOException e) {
			e.printStackTrace();
		}
    	
    	//Check if Employee Exists and returns error message if not found
    	if (!found) {
    	    System.out.println("Employee does not exist.");
    	    return;
    	}
    	
    	// Prints the information if the employee was found
    	System.out.println("===================================");
    	System.out.println("Employee # : " + employeeNumber);
    	System.out.println("Employee Name : " + lastName + ", " + firstName);
    	System.out.println("Birthday : " + employeeBirthday);
    	//System.out.println("Hourly Rate: " + hourlyRate); //testing if the hourly rate is correct per employee
    	System.out.println("===================================");
    	
    	//Formats the time to Hours:minutes
    	DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");
    	
    	//Process Attendance by Month for the year 2024 June - December
    	for (int month = 6; month <= 12; month++) {
    		double firstHalf = 0;
    		double secondHalf = 0;
    		int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();
    	
    	
    	//Read Attendance File
    	try (BufferedReader read = new BufferedReader(new FileReader(attendanceFile))) {
    		read.readLine();
    		String line;
    			while ((line = read.readLine()) != null){
    				if (line.trim().isEmpty()) continue;
    				String[] data = line.split(",");
    					if (!data[0].equals(employeeNumber))continue;
    					
    					//Date and Time 
    			    	String[] dateParts = data[3].split("/");
    			    	int recordMonth = Integer.parseInt(dateParts[0]);
    			    	int day = Integer.parseInt(dateParts[1]);
    			    	int year = Integer.parseInt(dateParts[2]);
    			    	
    			    	if (year != 2024 || recordMonth != month) continue;

    			    	LocalTime login = LocalTime.parse(data[4].trim(), timeFormat);
    			    	LocalTime logout = LocalTime.parse(data[5].trim(), timeFormat);
    			    	
    			    	double hours = computeHours(login, logout);
    			    	
    			    	if (day <= 15) firstHalf += hours;
    			        else secondHalf += hours;
    			}
    	}
    	catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	catch (IOException e) {
			e.printStackTrace();
		}
    	
    	//Calculate the Gross Salary
    	double firstHalfGross = firstHalf * hourlyRate;
    	double secondHalfGross = secondHalf * hourlyRate;
		double totalGross = firstHalfGross + secondHalfGross;

    	
    	//Calculate Deductions 
    	
	    	//SSS
	    	double deductionSSS = computeSSS(totalGross);    	
	    	
	    	//PhilHealth
	    	double deductionPhilhealth = computePhilheath(totalGross)/2;    	
	    	
	    	//Pag-IBIG
	    	double deductionPagibig = computePagibig(totalGross);
	    	
	    	//Calculate the sum of all deductions
	    	double initialDeduction = deductionSSS + deductionPhilhealth + deductionPagibig;
    	
    	//Calculate Taxes
			
    		//Calculate the Taxable Income
			double taxableSalary = totalGross - initialDeduction; 
	    	double tax = computeTax(taxableSalary);
	    
	    //Calculate the sum of all deductions WITH taxes
	    	double totalDeduction = initialDeduction + tax;

	    //Calculate the Net Salary
    	double netSalary = (totalGross - totalDeduction) - firstHalfGross; 
    	
    	//Use switch case to show "text format" of the month from CSV
		String monthName = switch (month) {
		case 1 -> "January";
		case 2 -> "February";
		case 3 -> "March";
		case 4 -> "April";
		case 5 -> "May";
		case 6 -> "June";
		case 7 -> "July";
		case 8 -> "August";
		case 9 -> "September";
		case 10 -> "October";
		case 11 -> "November";
		case 12 -> "December";
		default -> "Month " + month;
		};
		
		//Print hours worked for both cutoff
		System.out.println("\nCutoff Date: " + monthName + " 1 to 15");
		System.out.println("Total Hours Worked : " + firstHalf);
		System.out.println("Gross Salary: " + firstHalfGross);
		System.out.println("Net Salary: " + firstHalfGross);
		
		System.out.println("\nCutoff Date: " + monthName + " 16 to " + daysInMonth);
		System.out.println("Total Hours Worked : " + secondHalf);
		System.out.println("Gross Salary: " + secondHalfGross);
		System.out.println("Deductions: " + totalDeduction);
		System.out.println("    SSS: " + deductionSSS);
		System.out.println("    PhilHealth: " + deductionPhilhealth);
		System.out.println("    Pag-IBIG: " + deductionPagibig);
		System.out.println("    Tax: " + tax);
		System.out.println("Net Salary: " + netSalary);
		
		//For testing purposes - will delete later
		/*
		System.out.println("\n" + monthName + " Monthly Summary");
		System.out.println("Gross Salary: " + totalGross);
		System.out.println("Total Deductions: " + totalDeduction);
		System.out.println("Taxable Income: " + taxableSalary);
		System.out.println("Tax: " + tax);
		System.out.println("Net Salary: " + netSalary);
		 */
		
    	}
    } //end of main method

//List of all computations/methods used

    //Method that will compute hours worked
	static double computeHours(LocalTime login, LocalTime logout) {
				
			LocalTime graceTime = LocalTime.of(8, 10);
			LocalTime cutoffTime = LocalTime.of(17,0);
				
			// Apply 5PM cutoff
			if (logout.isAfter(cutoffTime)) {
				logout = cutoffTime;
			}
				
			long minutesWorked = Duration.between(login, logout).toMinutes();
				
			// Deduct lunch (if total worked is more than 1 hour)
			if (minutesWorked > 60) {
				minutesWorked -= 60;
			} else {
				minutesWorked = 0;
			}
				
			double hours = minutesWorked / 60.0;
				
			// Grace period rule
			if (!login.isAfter(graceTime)) {
				return 8.0;
			}
				
			// Return hours worked, capped at 8
			return Math.min(hours, 8.0);		
	    }
	
	//Method that will compute SSS Contributions
	static double computeSSS(double salary) {
	    
		if (salary <= 3250) {
	    	return 135.0;
	    } else if (salary >= 24750) {
	    	return 1125.0;
	    }
	    	return (Math.ceil((salary - 3250) / 500) * 22.5) + 135.0;
	}
	
	//Method that will compute PhilHealth CContributions
	static double computePhilheath (double salary) {
		double premium = salary * 0.03;
		
		if (salary <= 10000) {
			premium = 300;
		} else if (salary > 60000){
			premium = 1800;
		}
			return premium;
	}
	
	//Method that will compute Pag-Ibig Contributions
	static double computePagibig (double salary) {
		double employeeContribution;
		
		if (salary < 1000) {
			employeeContribution = 0;
		} else if (salary >= 1000 && salary <= 1500) {
			employeeContribution = salary * 0.01;
		} else {
			employeeContribution = salary * 0.02;
		} 
		
		if (employeeContribution > 100) {
			employeeContribution = 100;
		}
			return employeeContribution;	
	}
	
	//Method that will compute Tax Deductions 
	static double computeTax(double taxableSalary) {
		
	    if (taxableSalary <= 20833) {
	    	return 0;
	    } else if (taxableSalary <= 33332) {
	    	return 				(taxableSalary - 20833) * 0.20;
	    } else if (taxableSalary <= 66666) {
	    	return 2500 + 		(taxableSalary - 33332) * 0.25;
	    } else if (taxableSalary <= 166666) {
	    	return 10833 + 		(taxableSalary - 66667) * 0.30;
	    } else if (taxableSalary <= 666666) {
	    	return 40833.33 + 	(taxableSalary - 166666) * 0.32;
	    } else if (taxableSalary >= 666667) {
	    	return 200833.33 + 	(taxableSalary - 666666) * 0.35;
	    }
	    	return taxableSalary;
	}

}