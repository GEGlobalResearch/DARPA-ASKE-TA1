package com.ge.research.sadl.darpa.aske.prolog;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.ConfigurationManager;
import com.ge.research.sadl.reasoner.IConfigurationManager;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;

public class PrologReasonerTests {
	
	private String kbRoot = null;
	private String projectModelFolder;
	
	@Before
	public void setUp() throws Exception {
		File projectRoot = new File("resources/ASKE_P2");
		setKbRoot(projectRoot.getCanonicalPath());
		File prjFolder = new File(getKbRoot());
		assertTrue(prjFolder.exists());
		setProjectModelFolder(getKbRoot() + "/OwlModels");
	}


	@Test
	public void test() throws ConfigurationException, ReasonerNotFoundException, IOException, QueryParseException, QueryCancelledException {
		IConfigurationManager configMgr = new ConfigurationManager(getProjectModelFolder(), null);
		String prologReasonerClassName = "com.ge.research.sadl.swi_prolog.reasoner.SWIPrologReasonerPlugin";
		IReasoner pr = configMgr.getOtherReasoner(prologReasonerClassName);
		assertNotNull(pr);
		String instanceDatafile = "http://aske.ge.com/MetaData.owl";
		assertEquals(1, pr.initializeReasoner(getKbRoot(), instanceDatafile, null));
		
//		assertTrue(pr.loadInstanceData(instanceDatafile));
		assertTrue(loadAllOwlFilesInProject(getProjectModelFolder(), pr));
		String query = "select Y Z where holds(X,Y,Z)";
		ResultSet rs = pr.ask(query );
		assertNotNull(rs);
		System.out.println(rs.toString());
		
		
	}
	
	private boolean loadAllOwlFilesInProject(String owlModelsFolder, IReasoner reasoner) throws IOException, ConfigurationException {
		String[] excludeFiles = {"CodeExtractionModel.owl", "metrics.owl"};
		File omf = new File(owlModelsFolder);
		if (!omf.exists()) {
			throw new IOException("Model folder '" + owlModelsFolder + "' does not exist");
		}
		if (!omf.isDirectory()) {
			throw new IOException("'" + owlModelsFolder + "' is not a folder");
		}
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(java.io.File dir, String name) {
				if(name != null) {
					return name.endsWith(".owl");
				}else {
					return false;
				}
			}
		};
		File[] owlFiles = omf.listFiles(filter);
		for (File owlFile : owlFiles) {
			boolean exclude = false;
			for (String exf : excludeFiles) {
				if (owlFile.getName().endsWith(exf)) {
					exclude = true;
					break;
				}
			}
			if (exclude) {
				continue;
			}
			reasoner.loadInstanceData(owlFile.getCanonicalPath());
		}
		return true;
	}


	private String getKbRoot() {
		return kbRoot;
	}


	private void setKbRoot(String kbRoot) {
		this.kbRoot = kbRoot;
	}


	private String getProjectModelFolder() {
		return projectModelFolder;
	}


	private void setProjectModelFolder(String projectModelFolder) {
		this.projectModelFolder = projectModelFolder;
	}

}
