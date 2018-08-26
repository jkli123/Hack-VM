package com.chee.hackvm;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class VM {

	private CodeWriter codeWriter;
	private ArrayList<File> VMFiles;
	
	public VM(Path path) {
		VMFiles = extractVMFiles(path);
		System.out.println(VMFiles.size() + " VM files found in directory.");
		codeWriter = new CodeWriter(path);
	}
	
	private ArrayList<File> extractVMFiles(Path dir) {
		ArrayList<File> result = new ArrayList<>();
		File file = dir.toFile();
		File[] fileList = file.listFiles();
		for(File f : fileList) {
			if(f.getName().endsWith("vm")) {
				result.add(f);
			}
		}
		return result;
	}

	private VMParser initParser(File file) {
		VMParser parser = new VMParser(file.toPath());
		return parser;
	}
	
	private void fileTranslate(File file) {
		VMParser parser = initParser(file);
		codeWriter.updateFileName(file.getName().replace(".vm", ""));
		while(parser.hasMoreCommands()) {
			parser.advance();
			if(parser.commandType().equals("CARITHMETIC")) {
				codeWriter.writeArithmetic(parser.arg1());
			} else if(parser.commandType().equals("CPUSH")) {
				codeWriter.writePushPop("push", parser.arg1(), parser.arg2());
			} else if(parser.commandType().equals("CPOP")) {
				codeWriter.writePushPop("pop", parser.arg1(), parser.arg2());
			} else if(parser.commandType().equals("CLABEL")) {
				codeWriter.writeLabel(parser.arg1());
			} else if(parser.commandType().equals("CGOTO")) {
				codeWriter.writeGoto(parser.arg1());
			} else if(parser.commandType().equals("CIF")) {
				codeWriter.writeIf(parser.arg1());
			} else if(parser.commandType().equals("CCALL")) {
				codeWriter.writeCall(parser.arg1(), parser.arg2());
			} else if(parser.commandType().equals("CRETURN")) {
				codeWriter.writeReturn();
			} else if(parser.commandType().equals("CFUNCTION")) {
				codeWriter.writeFunction(parser.arg1(), parser.arg2());
			}
		}
	}
	
	public void translate() {
		codeWriter.writeInit();
		for(File file : VMFiles) {
			fileTranslate(file);
			System.out.println(file.getName() + " translation complete");
		}
		codeWriter.close();
		System.out.println("Whole translation complete");
	}
	
	public static void main(String[] args) {
		String pathDir = "W:\\Chee Peng\\Desktop\\Comp Sci\\NAND2Tetris\\Hardware Simulator\\nand2tetris\\projects\\08\\FunctionCalls\\StaticsTest";
		Path path = Paths.get(pathDir);
		VM vm = new VM(path);
		vm.translate();
	}
}
