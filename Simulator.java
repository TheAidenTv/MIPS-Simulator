import java.io.*;
import java.util.*;

// This code is a MIPS Simulator that can run a MIPS assembly file
// The simulator can run all instructions, step through instructions, dump memory, view register contents, modify register contents, and see the current instruction and PC
// The simulator can also handle comments and blank lines in the assembly file
// The simulator can also handle negative numbers
class Simulator {
    // Stores words as memory
    static String[] memory;
    static int maxMemorySize = 1000;

    // Program Counter
    static int PC;

    // Standard MIPS Registers
    static int[] registers;
    static String[] registerNames;

    // Placeholders
    static String opcode, rs, rt, rd, shamt, funct, immediate;

    // Main Method
    public static void main(String[] args) throws Exception {
        // Instantiate the memory location
        memory = new String[maxMemorySize];

        registerNames = new String[32];
        registerNames[0] = "$zero";
        registerNames[1] = "$at";
        registerNames[2] = "$v0";
        registerNames[3] = "$v1";
        registerNames[4] = "$a0";
        registerNames[5] = "$a1";
        registerNames[6] = "$a2";
        registerNames[7] = "$a3";
        registerNames[8] = "$t0";
        registerNames[9] = "$t1";
        registerNames[10] = "$t2";
        registerNames[11] = "$t3";
        registerNames[12] = "$t4";
        registerNames[13] = "$t5";
        registerNames[14] = "$t6";
        registerNames[15] = "$t7";
        registerNames[16] = "$s0";
        registerNames[17] = "$s1";
        registerNames[18] = "$s2";
        registerNames[19] = "$s3";
        registerNames[20] = "$s4";
        registerNames[21] = "$s5";
        registerNames[22] = "$s6";
        registerNames[23] = "$s7";
        registerNames[24] = "$t8";
        registerNames[25] = "$t9";
        registerNames[26] = "$k0";
        registerNames[27] = "$k1";
        registerNames[28] = "$gp";
        registerNames[29] = "$sp";
        registerNames[30] = "$fp";
        registerNames[31] = "$ra";

        // Create new Registers
        registers = new int[32];
        registers[29] = maxMemorySize - 1;

        // ASK FOR WHAT FILE TO LOAD
        Scanner userInputReader = new Scanner(System.in);
        System.out.println("Please enter the name of the file you wish to read including extensions");
        String path = userInputReader.nextLine();

        // Read the file and store it into memory
        loadFile(path);
        System.out.println("File successfully loaded\n");

        // Present the user with simulator options
        String userInput = "";

        do {
            System.out.println("Please type the action you wish to perform");
            System.out.println("RUN: Run all instructions");
            System.out.println("STEP: Run one instruction");
            System.out.println("DUMP: Perform a memory dump");
            System.out.println("VIEW: View register contents");
            System.out.println("MODIFY: Modify a register value");
            System.out.println("BREAK: Set a breakpoint and run until that breakpoint");
            System.out.println("SEE: See the current instruction and PC");
            System.out.println("QUIT: Exit the simulator");

            userInput = userInputReader.nextLine();

            System.out.println(); // Empty Spacing
            switch (userInput) {
                case "RUN":
                    runAll();
                    break;

                case "STEP":
                    execute();
                    break;

                case "DUMP":
                    System.out.println("What location do you wish to start the memory dump");
                    int location = userInputReader.nextInt();
                    userInputReader.nextLine(); // Clear the buffer
                    memoryDump(location);
                    break;

                case "VIEW":
                    printRegisterContents();
                    break;

                case "MODIFY":
                    System.out.println("Which register do you wish to modify");
                    int reg = userInputReader.nextInt();

                    while (reg == 0) {
                        System.out.println("You cannot modify register 0");
                        reg = userInputReader.nextInt();
                    }

                    System.out.println("What value do you wish to set it to");
                    int val = userInputReader.nextInt();
                    userInputReader.nextLine(); // Clear the buffer
                    registers[reg] = val;
                    break;

                case "SEE":
                    System.out.println("PC: " + PC);
                    System.out.println("Instruction: " + memory[PC]);
                    break;
                
                case "BREAK":
                    System.out.println("What location do you wish to set a breakpoint");
                    int breakPoint = userInputReader.nextInt();
                    userInputReader.nextLine(); // Clear the buffer
                    runToBreakpoint(breakPoint);
                    break;

                default:
                    userInput = "QUIT";

            }

            System.out.println(); // Empty Spacing
        } while (!userInput.equals("QUIT"));

        System.out.println("Simulator Stopping...");
        userInputReader.close();
    }

    // Run All - Runs every instruction in memory
    static void runAll() throws Exception {
        while (memory[PC] != null) {
            execute();
        }
    }

    // Run To Breakpoint - Runs instructions until it reaches a breakpoint
    static void runToBreakpoint(int breakpoint) throws Exception {
        while (PC != breakpoint) {
            execute();
        }

        System.out.println("Breakpoint Reached. Now at PC: " + PC);
    }

    static void loadFile(String path) throws Exception 
    {
        // Reading the file and storing the instructions in the array
        File file = new File(path);
        Scanner fileRead = new Scanner(file);
        int i;

        if (fileRead.hasNextLine()) 
        {
            i = fileRead.nextInt();
            PC = i;
            fileRead.nextLine();

            if (i >= maxMemorySize) 
            {
                fileRead.close();
                throw new Exception("Starting location out of bounds");
            }
        } 
        else 
        {
            fileRead.close();
            throw new Exception("This file is empty.");
        }

        while (fileRead.hasNextLine()) 
        {
            String line = fileRead.nextLine();
            if (line.contains("#")) 
            {
                line = line.substring(0, line.indexOf("#")).trim(); // remove comments
            }
            String[] tokens = line.split("\\s+");
            memory[i] = String.join("", tokens);
            i++;
        }

        fileRead.close();
    }

    // Print out 200 memory locations with 4 locations per line
    static void memoryDump(int startingAddy) {
        for (int i = 0; i < 50; i++) {
            System.out.printf("%5d:\t %32s\t %32s\t %32s\t %32s\t\n", startingAddy, memory[startingAddy],
                    memory[startingAddy + 1], memory[startingAddy + 2], memory[startingAddy + 3]);

            startingAddy += 4;
        }
    }

    // Print Register Contents
    static void printRegisterContents() {
        for (int i = 0; i < registers.length; i++) {
            System.out.printf("%s: %5d\n", registerNames[i], registers[i]);
        }
    }

    static void execute() throws Exception {
        String instruction = memory[PC];
        opcode = instruction.substring(0, 6);
        // System.out.println("OPCODE: " + opcode);

        rs = instruction.substring(6, 11);
        int rsVal = Integer.parseInt(rs, 2);
        // System.out.println("RS: " + rs);
        // System.out.println("RS: " + rsVal);

        rt = instruction.substring(11, 16);
        int rtVal = Integer.parseInt(rt, 2);
        // System.out.println("RT: " + rt);
        // System.out.println("RT: " + rtVal);

        rd = instruction.substring(16, 21);
        int rdVal = Integer.parseInt(rd, 2);

        immediate = instruction.substring(16, 32);
        int immVal = Integer.parseInt(immediate, 2);
        // System.out.println("Immediate: " + immediate);
        // System.out.println("Immediate: " + immVal);

        // TYPE R INSTRUCTION
        if (opcode.equals("000000")) {
            shamt = instruction.substring(21, 26);
            int shamtVal = Integer.parseInt(shamt, 2);

            funct = instruction.substring(26, 32);

            switch (funct) {
                // SLL
                case "000000":
                    sll(rdVal, rtVal, shamtVal);
                    break;

                // SRL
                case "000010":
                    if(shamtVal == 0)
                        mul(rdVal, rsVal, rtVal);

                    else
                        srl(rdVal, rtVal, shamtVal);
                    break;

                //JR
                case "001000":
                    jr(rsVal);
                    break;

                // ADD RS, RT, RD
                case "100000":
                    add(rdVal, rsVal, rtVal);
                    break;

                // SUB
                case "100010":
                    sub(rdVal, rsVal, rtVal);
                    break;

                // AND
                case "100100":
                    and(rdVal, rsVal, rtVal);
                    break;

                // OR
                case "100101":
                    or(rdVal, rsVal, rtVal);
                    break;

                // NOR
                case "100111":
                    nor(rdVal, rsVal, rtVal);
                    break;

                // XOR
                case "100110":
                    xor(rdVal, rsVal, rtVal);
                    break;

                //SLT
                case "101010":
                    slt(rdVal, rsVal, rtVal);
                    break;

                //SYSCALL
                case "001100":
                    syscall();
                    break;
                
                default:
                    throw new Exception(instruction + " is not a valid instruction");

            }
        }

        else // I and J's
        {
            String addr = instruction.substring(6, 32);
            int addrVal = Integer.parseInt(addr, 2);

            switch (opcode) 
			{
                //J
                case "000010":
                    j(addrVal);
                    break;
                
                //JAL
                case "000011":
                    jal(addrVal);
                    break;

                // ADDI
                case "001000":
                    addi(rtVal, rsVal, immVal);
                    break;

                // ANDI
                case "001100":
                    andi(rtVal, rsVal, immVal);
                    break;

                //SLTI
                case "001010":
                    slti(rdVal, rsVal, immVal);
                    break;

                // ORI
                case "001101":
                    ori(rtVal, rsVal, immVal);
                    break;

                // XORI
                case "001110":
                    xori(rtVal, rsVal, immVal);
                    break;

                //LI
                case "001111":
                    li(rsVal, immVal);
                    break;
                
                // SW
                case "101011":
                    sw(rtVal, rsVal, immVal);
                    break;
                
                // LW
                case "100011":
                    lw(rtVal, rsVal, immVal);
                    break;

                default:
                throw new Exception(instruction + " is not a valid instruction");
            }
        }

        PC++;

    }

    // MIPS METHOD CALLS

    // ADD RD, RS, RT
    static void add(int rd, int rs, int rt) {
        registers[rd] = registers[rs] + registers[rt];
    }

    // ADDI RT, RS, Immediate
    static void addi(int rt, int rs, int imm) {
        if (imm > 32767) {
            imm = -(65536 - imm); // Convert to signed
        }
        registers[rt] = registers[rs] + imm;
    }

    static void sub(int rd, int rs, int rt) {
        registers[rd] = registers[rs] - registers[rt];
    }
    
    //MUL (without overflow)
    static void mul(int rd, int rs, int rt)
    {
        registers[rd] = registers[rs] * registers[rt];
    }

    static void and(int rd, int rs, int rt) {
        registers[rd] = registers[rs] & registers[rt];
    }

    static void andi(int rt, int rs, int imm) {
        if (imm > 32767) {
            imm = -(65536 - imm); // Convert to signed
        }
        registers[rt] = registers[rs] & imm;
    }

    static void or(int rd, int rs, int rt) {
        registers[rd] = registers[rs] | registers[rt];
    }

    static void ori(int rt, int rs, int imm) {
        if (imm > 32767) {
            imm = -(65536 - imm); // Convert to signed
        }
        registers[rt] = registers[rs] | imm;
    }

    static void nor(int rd, int rs, int rt) {
        registers[rd] = ~(registers[rs] | registers[rt]);
    }

    static void xor(int rd, int rs, int rt) {
        registers[rd] = registers[rs] ^ registers[rt];
    }

    static void xori(int rt, int rs, int imm) {
        if (imm > 32767) {
            imm = -(65536 - imm); // Convert to signed
        }
        registers[rt] = registers[rs] ^ imm;
    }

    // shift left logical
    static void sll(int rd, int rt, int shamt) {
        registers[rd] = registers[rt] << shamt;
    }

    // shift right logical
    static void srl(int rd, int rt, int shamt) {
        registers[rd] = registers[rt] >>> shamt;
    }

    static void syscall() throws Exception
    {
        switch (registers[2]) 
        {
            // PRINT INT
            case 1:
                System.out.println(registers[4]);
                break;

            // READ INT
            case 5:
                Scanner scan = new Scanner(System.in);
                registers[2] = scan.nextInt();

            // EXIT
            case 10:
                System.out.println("Syscall exit... ending Program");
                System.exit(0);
                break;

            default:
                throw new Exception(registers[2] + " is not a valid syscall code");
        }
    }

	//Jump
	static void j(int addr)
	{
		PC = addr;
	}

    // Jump and Save Return
    static void jal(int addr) {
        registers[31] = PC + 1;
        PC = addr;
    }

	static void jr(int rs)
	{
		PC = rs;
	}

	static void slt(int rd, int rs, int rt)
	{
		if(rs < rt)
			registers[rd] = 1;
		
		else
			registers[rd] = 0;
	}

	static void slti(int rd, int rs, int imm)
	{
		if(rs < imm)
			registers[rd] = 1;
		
		else
			registers[rd] = 0;
	}

    // Load Word
    static void lw(int rt, int rs, int imm) {
        if (imm > 32767) {
            imm = -(65536 - imm); // Convert to signed
        }

        imm = imm / 4;
        registers[rt] = Integer.parseInt(memory[rs + imm], 2);
    }

    // Load Immediate
    static void li(int rd, int imm)
    {
        if (imm > 32767) 
        {
            imm = -(65536 - imm); // Convert to signed
        }
        registers[rd] = imm;
    }

    // Store Word
    static void sw(int rt, int rs, int imm) {
        if (imm > 32767) {
            imm = -(65536 - imm); // Convert to signed
        }

        imm = imm / 4;
        memory[rs + imm] = Integer.toBinaryString(registers[rt]);
    }
}