package com.chee.hackvm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class CodeWriter {

	private BufferedWriter writer;
	protected Path outputPath;
	protected String fileName;
	private HashMap<String, Integer> jumpRegisters;
	private String currentFunc;
	private int recursionNo = 0;
	
	public CodeWriter(Path path) {
		String pathName =  path.toString();
		if(pathName.contains(".vm")) {
			pathName = pathName.replace(".vm", ".asm");	
		} else {
			String filePath = path.getFileName().toString();
			outputPath = path.resolve(filePath.concat(".asm"));
		}
		setFileName(outputPath.toString());
		outputPath = Paths.get(pathName);
		jumpRegisters = new HashMap<>(7);
		jumpRegisters.put("JGT", 0);
		jumpRegisters.put("JEQ", 0);
		jumpRegisters.put("JGE", 0);
		jumpRegisters.put("JLT", 0);
		jumpRegisters.put("JNE", 0);
		jumpRegisters.put("JLE", 0);
		jumpRegisters.put("JMP", 0);
		currentFunc = "null";
	}
	
	public void updateFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setFileName(String fileName) {
		Path output = Paths.get(fileName);
		outputPath = output;
		this.fileName = outputPath.getFileName().toString().replace(".asm", "");
		try {
			writer = Files.newBufferedWriter(output);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private String atSP() {
		return "@SP\n";
	}
	
	private String atLCL() {
		return "@LCL\n";
	}
	
	private String atARG() {
		return "@ARG\n";
	}
	
	private String atTHIS() {
		return "@THIS\n";
	}
	
	private String atTHAT() {
		return "@THAT\n";
	}
	
	private String memLocationEquals() {
		return "M=";
	}
	
	private String DRegisterEquals() {
		return "D=";
	}
	
	private String ARegisterEquals() {
		return "A=";
	}
	
	private String memLocation() {
		return "M";
	}
	
	private String DRegister() {
		return "D";
	}
	
	private String ARegister() {
		return "A";
	}
	
	private String minusOne() {
		return "-1";
	}
	
	private String plusOne() {
		return "+1";
	}
	
	private String atRAM(int location) {
		location += 13;
		return "@R" + location + "\n";
	}
	
	private String atIndex(int index) {
		return "@" + index + "\n";
	}
	
	private String increaseSP() {
		String output = "";
		output += atSP();
		output += memLocationEquals() + memLocation() + plusOne() + "\n";
		return output;
	}
	
	private String decreaseSP() {
		String output = "";
		output += atSP();
		output += memLocationEquals() + memLocation() + minusOne() + "\n";
		return output;
	}
	
	private String storeValueInDRegister(int value) {
		String output = "";
		output += atIndex(value);
		output += DRegisterEquals() + ARegister() + "\n";
		return output;
	}
	
	private String storeMemoryValueInDRegister() {
		String output = "";
		output += DRegisterEquals() + memLocation() + "\n";
		return output;
	}
	
	private String storeDValueInRAM(int location) {
		String output = "";
		output += atRAM(location);
		output += memLocationEquals() + DRegister() + "\n";
		return output;
	}
	
	private String followSP() {
		String output = "";
		output += atSP();
		output += ARegisterEquals() + memLocation() + "\n";
		return output;
	}
	private String pushDRegisterValueOntoStack() {
		String output = "";
		output += followSP();
		output += memLocationEquals() + DRegister() + "\n";
		output += increaseSP();
		return output;
	}
	
	private String popAndStoreInDRegisterAndRAM(int location) {
		String output = "";
		output += decreaseSP();
		output += followSP();
		output += storeMemoryValueInDRegister();
		output += storeDValueInRAM(location);
		return output;
	}
	
	private String popAndStoreInDRegister() {
		String output = "";
		output += decreaseSP();
		output += followSP();
		output += storeMemoryValueInDRegister();
		return output;
	}
	
	private String subtractValueOf(int numberMem, int withMem) {
		String output = "";
		output += atRAM(numberMem);
		output += storeMemoryValueInDRegister();
		output += atRAM(withMem);
		output += DRegisterEquals() + DRegister() + "-" + memLocation() + "\n";
		return output;
	}
	
	private String negativeValueOf(int numMem) {
		String output = "";
		output += atRAM(numMem);
		output += memLocationEquals() + "-" + memLocation() + "\n";
		output += DRegisterEquals() + memLocation() + "\n";
		return output;
	}
	
	private void writeOutputToFile(String output) {
		try {
			writer.write(output);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private String add() {
		String output = "";
		output += popAndStoreInDRegisterAndRAM(0);
		output += decreaseSP();
		output += followSP();
		output += storeMemoryValueInDRegister();
		output += atRAM(0);
		//Adds D reg value with RAM 0 value and stores in D reg for transport
		output += DRegisterEquals() + DRegister() + "+" + memLocation() + "\n";
		output += pushDRegisterValueOntoStack();
		return output;
	}
	
	private String subtract() {
		String output = "";
		output += popAndStoreInDRegisterAndRAM(0);
		output += popAndStoreInDRegisterAndRAM(1);
		output += subtractValueOf(1, 0);
		output += pushDRegisterValueOntoStack();
		return output;
	}
	
	private String negative() {
		String output = "";
		output += popAndStoreInDRegisterAndRAM(0);
		output += negativeValueOf(0);
		output += pushDRegisterValueOntoStack();
		return output;
	}
	
	private String jumpDRegister(String jumpCond) {
		String output = "";
		String jumpCondUpper = jumpCond.toUpperCase();
		int jumpReg = jumpRegisters.get(jumpCondUpper);
		output += "@" + jumpCondUpper + "_JUMP_";
		output += jumpReg + "\n";
		output += "D;" + jumpCondUpper + "\n";
		return output;
	}
	
	private String land(String jumpCond) {
		String output = "";
		String jumpCondUpper = jumpCond.toUpperCase();
		int jumpReg = jumpRegisters.get(jumpCondUpper);
		output += "(" + jumpCondUpper + "_JUMP_" + jumpReg + ")\n";
		jumpReg++;
		jumpRegisters.put(jumpCondUpper, jumpReg);
		return output;
	}
	
	private String pushValueMinusOneOntoStack(int location) {
		String output = "";
		output += atRAM(location);
		output += storeMemoryValueInDRegister();
		output += DRegisterEquals() + DRegister() + minusOne() + "\n";
		output += pushDRegisterValueOntoStack();
		return output;
	}

	private String storeTrueFalseAt(int trueloc, int falseloc) {
		String output = "";
		output += storeValueInDRegister(0);
		output += storeDValueInRAM(trueloc);
		output += storeValueInDRegister(1);
		output += storeDValueInRAM(falseloc);
		return output;
	}

	private String equal() {
		String output = "";
		output += subtract();
		output += storeTrueFalseAt(0, 1);
		output += popAndStoreInDRegisterAndRAM(2);
		output += jumpDRegister("JEQ");
		output += jumpDRegister("JNE");
		output += land("JEQ");
		output += pushValueMinusOneOntoStack(0);
		output += jumpDRegister("JMP");
		output += land("JNE");
		output += pushValueMinusOneOntoStack(1);
		output += jumpDRegister("JMP");
		output += land("JMP");
		return output;
	}
	
	private String greater() {
		String output = "";
		output += subtract();
		output += storeTrueFalseAt(0, 1);
		output += popAndStoreInDRegisterAndRAM(2);
		output += jumpDRegister("JGT");
		output += jumpDRegister("JLE");
		output += land("JGT");
		output += pushValueMinusOneOntoStack(0);
		output += jumpDRegister("JMP");
		output += land("JLE");
		output += pushValueMinusOneOntoStack(1);
		output += jumpDRegister("JMP");
		output += land("JMP");
		return output;
	}
	
	private String less() {
		String output = "";
		output += subtract();
		output += storeTrueFalseAt(0, 1);
		output += popAndStoreInDRegisterAndRAM(2);
		output += jumpDRegister("JLT");
		output += jumpDRegister("JGE");
		output += land("JLT");
		output += pushValueMinusOneOntoStack(0);
		output += jumpDRegister("JMP");
		output += land("JGE");
		output += pushValueMinusOneOntoStack(1);
		output += jumpDRegister("JMP");
		output += land("JMP");
		return output;
	}
	
	private String and() {
		String output = "";
		output += popAndStoreInDRegisterAndRAM(0);
		output += popAndStoreInDRegisterAndRAM(1);
		output += atRAM(0);
		output += DRegisterEquals() + DRegister() + "&" + memLocation() + "\n";
		output += pushDRegisterValueOntoStack();
		return output;
	}

	private String or() {
		String output = "";
		output += popAndStoreInDRegisterAndRAM(0);
		output += popAndStoreInDRegisterAndRAM(1);
		output += atRAM(0);
		output += DRegisterEquals() + DRegister() + "|" + memLocation() + "\n";
		output += pushDRegisterValueOntoStack();
		return output;
	}
	
	private String not() {
		String output = "";
		output += popAndStoreInDRegisterAndRAM(0);
		output += atRAM(0);
		output += DRegisterEquals() + "!" + memLocation() + "\n";
		output += pushDRegisterValueOntoStack();
		return output;
	}
	
	public void writeArithmetic(String command) {
		String output = "";
		switch(command) {
			case("add") :
				output = add();
				break;
			case("sub") :
				output = subtract();
				break;
			case("neg") :
				output = negative();
				break;
			case("eq") :
				output = equal();
				break;
			case("gt") :
				output = greater();
				break;
			case("lt") :
				output = less();
				break;
			case("and") :
				output = and();
				break;
			case("or") :
				output = or();
				break;
			case("not") :
				output = not();
				break;
		}
		writeOutputToFile(output);
	}
	
	private String accessSegmentIndex(String segment, int index) {
		String output = "";
		String segmentUpper = segment.toUpperCase();
		output += storeValueInDRegister(index);
		switch(segmentUpper) {
			case("LOCAL") :
				segmentUpper = "LCL";
				break;
			case("ARGUMENT") :
				segmentUpper = "ARG";
				break;
			case("TEMP") :
				segmentUpper = translateTempLocation(index);
				output += "@" + segmentUpper + "\n";
				output += DRegisterEquals() + ARegister() + "\n";
				return output;
			case("POINTER") :
				if(index == 0) {
					segmentUpper = "@THIS\n";
				} else {
					segmentUpper = "@THAT\n";
				}
				output += segmentUpper;
				output += DRegisterEquals() + ARegister() + "\n";
				return output;
			case("STATIC") :
				output += "@" + fileName + "." + index + "\n";
				output += DRegisterEquals() + ARegister() + "\n";
				return output;
		}
		output += "@" + segmentUpper;
		output += "\n" + DRegisterEquals() + memLocation() + "+" + DRegister() + "\n";
		return output;
	}
	
	private String translateTempLocation(int index) {
		return Integer.toString(index + 5);
	}
	
	private String pushConstantOntoStack(int constant) {
		String output = "";
		output += storeValueInDRegister(constant);
		output += pushDRegisterValueOntoStack();
		return output;
	}
	
	private String pushSegmentOntoStack(String segment, int index) {
		String output = "";
		if(segment.equals("constant")) {
			return pushConstantOntoStack(index);
		}
		output += accessSegmentIndex(segment, index);
		output += ARegisterEquals() + DRegister() + "\n";
		output += storeMemoryValueInDRegister();
		output += pushDRegisterValueOntoStack();
		return output;
	}
	
	private String popFromStackOnto(String segment, int index) {
		String output = "";
		output += accessSegmentIndex(segment, index);
		output += storeDValueInRAM(0);
		output += popAndStoreInDRegister();
		output += atRAM(0);
		output += ARegisterEquals() + memLocation() + "\n";
		output += memLocationEquals() + DRegister() + "\n";
		return output;
	}
	
	public void writePushPop(String command, String segment, int index) {
		String output = "";
		if(command.equals("push")) {
			output = pushSegmentOntoStack(segment, index);
		} else {
			output = popFromStackOnto(segment, index);
		}
		writeOutputToFile(output);
	}
	
	
	private String constructLabelName(String label) {
		String currentFuncName = currentFunc;
		return currentFuncName + "$" + label;
	}
	
	public void writeInit() {
		String output = "";
		output += storeValueInDRegister(256);
		output += atSP();
		output += memLocationEquals() + DRegister() + "\n";
		writeOutputToFile(output);
		writeCall("Sys.init", 0);
	}
	
	public void writeLabel(String label) {
		String output = "";
		output += "(" + constructLabelName(label) + ")" + "\n";
		writeOutputToFile(output);
	}
	
	public void writeGoto(String label) {
		String output = "";
		output += "@" + constructLabelName(label) + "\n";
		output += "0;JMP\n";
		writeOutputToFile(output);
	}
	
	public void writeIf(String label) {
		String output = "";
		output += popAndStoreInDRegister();
		output += "@" + constructLabelName(label) + "\n";
		output += "D;JNE\n";
		writeOutputToFile(output);
	}
	
	private String pushPredefSymbolsValueOntoStack(String symbol) {
		String output = "";
		output += "@" + symbol + "\n";
		output += storeMemoryValueInDRegister();
		output += pushDRegisterValueOntoStack();
		return output;
	}
	
	private String pushRetAddOntoStack(String funcName) {
		String output = "";
		output += "@" + constructLabelName(funcName) + "\n";
		output += DRegisterEquals() + ARegister() + "\n";
		output += pushDRegisterValueOntoStack();
		return output;
	}
	
	public void writeCall(String funcName, int numArgs) {
		String output = "";
		output += pushRetAddOntoStack("RETURN$" + recursionNo + "$" + funcName);
		output += pushPredefSymbolsValueOntoStack("LCL");
		output += pushPredefSymbolsValueOntoStack("ARG");
		output += pushPredefSymbolsValueOntoStack("THIS");
		output += pushPredefSymbolsValueOntoStack("THAT");
		output += storeValueInDRegister(numArgs);
		output += storeDValueInRAM(0);
		output += storeValueInDRegister(5);
		output += storeDValueInRAM(1);
		output += atSP();
		output += storeMemoryValueInDRegister();
		output += storeDValueInRAM(2);
		output += subtractValueOf(2, 0);
		output += storeDValueInRAM(0);
		output += subtractValueOf(0, 1);
		output += atARG();
		output += memLocationEquals() + DRegister() + "\n";
		output += atSP();
		output += storeMemoryValueInDRegister();
		output += atLCL();
		output += memLocationEquals() + DRegister() + "\n";
		output += "@" + constructFuncName(funcName) + "\n";
		output += "0;JMP\n";
		writeOutputToFile(output);
		writeLabel("RETURN$" + recursionNo + "$" + funcName);
		recursionNo++;
	}
	
	private String restoreValueOf(String symbol) {
		String output = "";
		switch(symbol) {
			case("SP") :
				output += atARG();
				output += storeMemoryValueInDRegister();
				output += DRegisterEquals() + DRegister() + plusOne() + "\n";
				output += atSP();
				output += memLocationEquals() + DRegister() + "\n";
				break;
			case("THAT") :
				output += atRAM(0);
				output += storeMemoryValueInDRegister();
				output += DRegisterEquals() + DRegister() + minusOne() + "\n";
				output += ARegisterEquals() + DRegister() + "\n";
				output += storeMemoryValueInDRegister();
				output += atTHAT();
				output += memLocationEquals() + DRegister() + "\n";
				break;
			case("THIS") :
				output += storeValueInDRegister(2);
				output += storeDValueInRAM(2);
				output += subtractValueOf(0, 2);
				output += ARegisterEquals() + DRegister() + "\n";
				output += storeMemoryValueInDRegister();
				output += atTHIS();
				output += memLocationEquals() + DRegister() + "\n";
				break;
			case("ARG") :
				output += storeValueInDRegister(3);
				output += storeDValueInRAM(2);
				output += subtractValueOf(0, 2);
				output += ARegisterEquals() + DRegister() + "\n";
				
				output += storeMemoryValueInDRegister();
				output += atARG();
				output += memLocationEquals() + DRegister() + "\n";
				break;
			case("LCL") :
				output += storeValueInDRegister(4);
				output += storeDValueInRAM(2);
				output += subtractValueOf(0, 2);
				output += ARegisterEquals() + DRegister() + "\n";
				output += storeMemoryValueInDRegister();
				output += atLCL();
				output += memLocationEquals() + DRegister() + "\n";
				break;
		}
		return output;
	}
	
	public void writeReturn() {
		String output = "";
		output += atLCL();
		output += storeMemoryValueInDRegister();
		//FRAME temporary variable stored in RAM 0
		output += storeDValueInRAM(0);
		output += storeValueInDRegister(5);
		output += storeDValueInRAM(1);
		output += subtractValueOf(0, 1);
		output += ARegisterEquals() + DRegister() + "\n";
		output += storeMemoryValueInDRegister();
		//RET temporary variable store in RAM 1
		output += storeDValueInRAM(1);
		output += popAndStoreInDRegister();
		output += atARG();
		output += ARegisterEquals() + memLocation() + "\n";
		output += memLocationEquals() + DRegister() + "\n";
		output += restoreValueOf("SP");
		output += restoreValueOf("THAT");
		output += restoreValueOf("THIS");
		output += restoreValueOf("ARG");
		output += restoreValueOf("LCL");
		output += atRAM(1);
		output += storeMemoryValueInDRegister();
		output += ARegisterEquals() + DRegister() + "\n";
		output += "0;JMP\n";
		writeOutputToFile(output);
	}
	
	private String constructFuncName(String funcName) {
		String output = "";
		output += funcName;
		return output;
	}
	
	public void writeFunction(String funcName, int numLocals) {
		currentFunc = funcName;
		String output = "";
		output += "(" + constructFuncName(funcName) + ")\n";
		output += storeValueInDRegister(0);
		for(int i = 0; i < numLocals; i++) {
			output += pushDRegisterValueOntoStack();
		}
		writeOutputToFile(output);
	}
	
	public void close() {
		try {
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
