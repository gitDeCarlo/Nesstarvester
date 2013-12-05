package org.openmetadata.ddiharvester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;
import org.junit.Test;
import org.openmetadata.harvester.Harvester;
import org.openmetadata.nesstar.HarvesterOptions;

public class harvesterTest {


	@Test
	public void testHarvester() throws Exception{

		String[] args = {"src/test/resources/harvester.properties"};
		//String[] args = {"src/test/resources/ukda.properties"};

		Harvester.main(args);

		//Delete files to create another full update
		//File ddiFolder = new File("/home/andrew/Nesstar/BetaCat");
		//delete(ddiFolder);

		//File database = new File("/home/andrew/BaseXData/BetaCat");
		//delete(database);

	}

	@Test
	public void createProperties() throws Exception{
		// create and load default properties
		Properties defaultProps = new Properties();
		FileInputStream in = new FileInputStream("src/test/resources/harvester.properties");
		defaultProps.load(in);
		in.close();

		// create application properties with default
		Properties applicationProps = new Properties(defaultProps);

		/*
		applicationProps.setProperty("serverUrl","http://compass-data.unil.ch");
		applicationProps.setProperty("portNumber","80");
		applicationProps.setProperty("outputFolder","/home/andrew/Nesstar/ch_fors_compass");
		applicationProps.setProperty("languageList","en");

		FileOutputStream out = new FileOutputStream("src/test/resources/harvester.properties");
		applicationProps.store(out, "---Harvester Options for Nesstarvester---");
		out.close();	
		 */

		System.out.println(applicationProps.getProperty("hello"));
	}

	public static void delete(File file) 	throws IOException{

		if(file.isDirectory()){

			//directory is empty, then delete it
			if(file.list().length==0){

				file.delete();
				System.out.println("Directory is deleted : " 
						+ file.getAbsolutePath());

			}else{

				//list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					//construct the file structure
					File fileDelete = new File(file, temp);

					//recursive delete
					delete(fileDelete);
				}

				//check the directory again, if empty then delete it
				if(file.list().length==0){
					file.delete();
					System.out.println("Directory is deleted : " 
							+ file.getAbsolutePath());
				}
			}

		}else{
			//if file, then delete it
			file.delete();
			System.out.println("File is deleted : " + file.getAbsolutePath());
		}
	}

	@Test 
	public void testserialization() throws Exception{
		HarvesterOptions options = new HarvesterOptions();
		options.setDb("test");

		String filename = "src/test/resources/options.ser";

		FileOutputStream byos = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(byos);
		oos.writeObject(options);


		FileInputStream bais = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(bais);
		HarvesterOptions options2 = (HarvesterOptions) ois.readObject();
		System.out.println(options2.getDb());
	}

	@Test
	public void testPackager() throws Exception{
		String one = "2013-04-15T17:41:44Z";
		String two = "2013-03-21T17:01:33Z";

		String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";


		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date dOne = sdf.parse(one);


		SimpleDateFormat sdf2 = new SimpleDateFormat(format);
		Date dTwo = sdf2.parse(two);

		System.out.println(dOne.after(dTwo));

		/*
		Packager packager = new Packager("src/test/resources/", "compressed", "ddi");
		packager.packageFiles();
		 */
	}

	@Test
	public void testQuery() throws Exception{

		Context context = new Context();

		new Open("ch_fors_compass").execute(context);

		InputStream in = getClass().getResourceAsStream("/nesstarvester_catalog.xq");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String xquery = "";
		String queryLine = "";

		//creates the xquery that in in src/main/resources/nesstarvester_catalog_changes.xq
		while((queryLine = br.readLine()) != null){
			xquery += queryLine;
		}
		br.close();

		String updates = (new XQuery(xquery).execute(context));
		System.out.println(updates);

		new Close().execute(context);
	}


	@Test
	public void testGetLang(){
		String retention = "1h";

		String retentionRegex = "[0-9]+";
		Pattern p = Pattern.compile(retentionRegex);
		Matcher m = p.matcher(retention);
		if(!m.matches()){
			Matcher m2 = p.matcher(retention);
			if(m2.find()){
				//split the retentionTime and find units
				String num = m2.group(0);
				String unit = retention.replaceFirst(num, "");

				System.out.println(num+ " | "+unit);
			}
			else{
				System.out.println("here");
			}
		}
		else{
			System.out.println("here");
		}
	}
}
