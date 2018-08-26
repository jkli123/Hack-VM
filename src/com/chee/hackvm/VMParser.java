package com.chee.hackvm;

import java.nio.file.Path;

import com.chee.hackassembler.Parser;

public class VMParser extends Parser{

	public VMParser(Path path) {
		super(path);
	}
	
	@Override
	public String commandType() {
		if(currentCommand.startsWith("label")) {
			return "CLABEL";
		} else if(currentCommand.startsWith("goto")) {
			return "CGOTO";
		} else if(currentCommand.startsWith("if-goto")) {
			return "CIF";
		} else if(currentCommand.startsWith("function")) {
			return "CFUNCTION";
		} else if(currentCommand.startsWith("call")) {
			return "CCALL";
		} else if(currentCommand.startsWith("return")) {
			return "CRETURN";
		} else if(currentCommand.startsWith("push")) {
			return "CPUSH";
		} else if(currentCommand.startsWith("pop")) {
			return "CPOP";
		} else if(currentCommand.startsWith("add") || currentCommand.startsWith("sub")
				||currentCommand.startsWith("neg") || currentCommand.startsWith("eq")
				||currentCommand.startsWith("gt") || currentCommand.startsWith("lt")
				||currentCommand.startsWith("and") || currentCommand.startsWith("or")
				||currentCommand.startsWith("not")) {
			return "CARITHMETIC";
		} else {
			return "CLABEL";
		}
	}
	
	public String arg1() {
		if(commandType() == "CRETURN") {
			return null;
		} else if(commandType() == "CARITHMETIC"){
			return currentCommand;
		} else {
			return currentCommand.split(" ")[1];
		}
	}
	
	public int arg2() {
		if(commandType() == "CPUSH" || commandType() == "CPOP"
				||commandType() == "CFUNCTION" || commandType() == "CCALL") {
			return Integer.parseInt(currentCommand.split(" ")[2]);
		} else {
			throw new IllegalStateException("Method not supposed to be called if command type does not match");
		}
	}
	
	@Override
	public void removeSpaces() {
		currentCommand = currentCommand.trim();
	}
}
