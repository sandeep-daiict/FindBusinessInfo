package com.web.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler 
{
	public static void main(String args[])
	{
		if(args[1]!=null)
		{
			System.getProperties().put("http.proxyHost", args[1]);
			System.getProperties().put("http.proxyPort", args[2]);
		}
		int numemail=0;
		//System.out.println(System.getProperties().get("http.proxyhost"));
		try {
			BufferedWriter brw = new BufferedWriter(new FileWriter(new File("result.txt")));
			BufferedWriter brwcurl = new BufferedWriter(new FileWriter(new File("curl.txt")));
			BufferedWriter brwlog = new BufferedWriter(new FileWriter(new File("log")));
		File folder = new File(args[0]);
		File[] files = folder.listFiles();
		int num = files.length;
		for(int i = 0 ; i < num ; i++)
		{
			System.out.println("i: " + i );
			BusinessInfo bi = new BusinessInfo(brwlog);
			brw.write(files[i].getName()+"\t");
			bi.crawl(files[i]);
			brw.write(bi.businessname+"\t");
			//System.out.println("Business name:"+bi.businessname);
			if(!bi.homeurl_present)
			{
				brw.write("NOHOMEURL"+"\n");
				continue;
			}
			if(bi.phones.size()>0)
				brw.write(bi.phones.get(0)+"\t");
			brw.write(bi.homeurl+"\t");
			
			bi.crawlHomePage();
			
			
			//System.out.println("Home Page URL:"+bi.);
			/*for (String phone : bi.phones) 
			{
				//System.out.println("phones:" + phone);
			}*/
			int numcurl=0;
			for (String curl : bi.contactusurl) 
			{
				numcurl++;
				if(numcurl==1)
					brw.write(curl+"\t");
				if(bi.emails.size()==0)
				{
					if(numcurl==1)
						brwcurl.write("filename:"+files[i].getName()+", name:"+bi.businessname+"\n");
					brwcurl.write(curl+"\n");
				}
				
			}
			for (String email : bi.emails) 
			{
				 numemail++;
				//we can use String distance here b/w business name and email id to find appropriate distance
				brw.write(email+"\t");
			}
			
			brw.write("\n");
			brw.flush();
			brwcurl.flush();
			brwlog.flush();
			
			//System.out.println("::::::::::::::::::::::;;");
		}
		brwlog.close();
		//crawllogagain(brw,brwlog); 
		brw.close();
		brwcurl.close();
		
		
		System.out.println("EMAIL: "+ numemail);
		crawlcontactusurl();
		merger();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
}

	private static void crawllogagain(BufferedWriter brw, BufferedWriter brwlog) 
	{ //crawl log again will crawl the pages which failed earlier
		Document doc;
		try {
		BufferedReader brlog = new BufferedReader(new FileReader(new File("log")));
		BufferedWriter brwe = new BufferedWriter(new FileWriter(new File("email.txt")));
		BufferedWriter brwp = new BufferedWriter(new FileWriter(new File("phone.txt")));
		BufferedWriter brwc = new BufferedWriter(new FileWriter(new File("contact.txt")));
		String line = new String();
		
			while((line=brlog.readLine())!=null)
			{
				
			
				String[] str = line.split(":");
				String url = str[2];
				String name=str[1];
			//System.out.println("url: "+url);
			
			boolean emailid_present = false;
			try
			{	
				Connection con  = Jsoup.connect(url);
				doc = con.get();
				String Text = doc.html();
				//System.out.println("Text:"+Text);
				//^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				//+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$
				//String regex= ".*(\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b).*";
				final Pattern pat = Pattern.compile("[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})", Pattern.CASE_INSENSITIVE);
				Matcher regexMatcher = pat.matcher(Text);
				
				
				
				while(regexMatcher.find())
				{
					
					brwe.write(name+":"+regexMatcher.group()+"\n");
					emailid_present=true;
					break;
				}
				
				
				final Pattern patphone = Pattern.compile("[+]?[01]?[-. ]?(\\(\\d{3}\\)|\\d{3})[ .-]\\d{3}[-. ]\\d{4}", Pattern.CASE_INSENSITIVE);
				Matcher regexMatcherphone = patphone.matcher(Text);
				
				
				
				while(regexMatcherphone.find())
				{
					
					brwp.write(name+":"+regexMatcherphone.group()+"\n");
				}
				//findLinks(doc);
				Elements links = doc.select("[href]");
				//System.out.println("size: "+links.size());
		        for (Element link : links) 
		        {
		        	String text = link.attr("abs:href");
		            if(text.toLowerCase().contains("contact") || text.toLowerCase().contains("about"))
		            {
		            	brwc.write(name+":"+text+"\n");
		            	break;
		            }	
		        	
				}
		        brwe.flush();
		        brwe.close();
		        brwc.flush();
		        brwc.close();
		        brwp.flush();
		        brwp.close();
				
				}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void merger() 
	{ 	
		try {
			BufferedReader brresult = new BufferedReader(new FileReader(new File("result.txt")));
			BufferedReader brwemail = new BufferedReader(new FileReader(new File("emailnew.txt")));
			
			BufferedWriter brresultnew = new BufferedWriter(new FileWriter(new File("resultnew.txt")));
			String Line = new String();
			
			TreeMap<String, String> map = new TreeMap<String, String>();
			
			while((Line=brresult.readLine())!=null)
			{
				System.out.println("line"+Line);
				String[] str =Line.split("\\t",2);
				map.put(str[0], str[1]);
				System.out.println("str0 : "+str[0]+ "str1 : "+str[1]);
			}
			while((Line=brwemail.readLine())!=null)
			{
				String[] str = Line.split(" ");
				if(map.containsKey(str[0]))
				{
					String val = map.get(str[0]);
					map.remove(str[0]);
					map.put(str[0], val+str[1]);
				}
			}
			brresult.close();
			brwemail.close();
			for(Map.Entry<String,String> entry : map.entrySet()) {
				  String key = entry.getKey();
				  String value = entry.getValue();

				  brresultnew.write(key+"\t"+value+"\n");
				}
			brresultnew.flush();
			
			brresultnew.close();
			File f = new File("resultnew");
			f.renameTo(new File("result"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private static void crawlcontactusurl() 
	{
		
		try{
			BufferedReader brcurl = new BufferedReader(new FileReader(new File("curl.txt")));
			BufferedWriter brwemail = new BufferedWriter(new FileWriter(new File("emailnew.txt")));
			String line=brcurl.readLine();
			while(line!=null)
			{
				int email = 0;
				String emailtext=new String();
				String name = line.split(":")[1].split(",")[0];
				while((line=brcurl.readLine())!=null)
				{
					System.out.println("line:" + line);
					if(!line.split(":")[0].equals("filename"))
					{
						if(email!=0)
						{
							continue;
						}
						else
						{
							try
							{
							Document doc;
							Connection con  = Jsoup.connect(line);
							doc = con.get();
							String Text = doc.html();
							final Pattern pat = Pattern.compile("[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})", Pattern.CASE_INSENSITIVE);
							Matcher regexMatcher = pat.matcher(Text);				
							
							while(regexMatcher.find())
							{
								
								emailtext=regexMatcher.group();
								email = 1;
								brwemail.write(name+" "+emailtext +"\n");
								brwemail.flush();
								break;
							}
							}
							catch(SocketTimeoutException e)
							{
								e.printStackTrace();
							}
							catch(IOException e1)
							{
								e1.printStackTrace();
							}
						}
					}
					else
						break;
				}
			}
			brcurl.close();
			brwemail.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}
}
