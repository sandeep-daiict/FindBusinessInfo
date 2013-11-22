package com.web.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class BusinessInfo 
{
	public String businessname ;
	public ArrayList<String> phones = new ArrayList<String>();
	public String homeurl;
	/*public String emailid;
	public String contactusurl;
	*/boolean businessname_present=false;
	boolean phone_present=false;
	boolean homeurl_present=false;
	boolean emailid_present=false;
	boolean contactusurl_present=false;
	String id;
	BufferedWriter brlog;
	HashSet<String> emails = new HashSet<String>();
	HashSet<String> contactusurl = new HashSet<String>();
	public BusinessInfo(BufferedWriter br)
	{
		businessname = new String();
		
		
		homeurl = new String();
		//emailid = new String();
		contactusurl = new  HashSet<String>();
		id = new String();
		brlog = br;
	}

	public void crawl(File file)
	{
				try 
				{
					
					Document doc = Jsoup.parse(file, "UTF-8");
					id = file.getName().split("\\.")[0];
					Elements bname = doc.getElementsByAttributeValue("itemprop", "name");
					if(bname!=null)
					{
						
						businessname = bname.text();
						businessname_present=true;
					}
					Element phonenum = doc.getElementById("bizPhone");
					if(phonenum!=null)
					{
						phones.add(phonenum.text());
						phone_present=true;
					}
					Element hurl = doc.getElementById("bizUrl");
					if(hurl!=null)
					{
						Elements hurla=hurl.select("a");
						if(hurla!=null)
						{							
							homeurl = hurla.text();
							homeurl_present=true;
						}
					}	
					//System.out.println("file:"+file.getName()+"  businessname:"+ businessname + " Phone :" + phone+ " HomeURL:" + homeurl);
					
					
				} 
				catch (IOException e) 
				{
					System.out.println("Error In Parsing");
					e.printStackTrace();
				}
				
	}
	
	public void crawlHomePage()
	{
		if(homeurl.isEmpty())
			return ;
		Document doc;
		String url = "http://www."+homeurl;
		//System.out.println("url: "+url);
		
		
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
				
				emails.add(regexMatcher.group());
			}
			if(!emails.isEmpty())
			{
				emailid_present=true;
			}
			
			final Pattern patphone = Pattern.compile("[+]?[01]?[-. ]?(\\(\\d{3}\\)|\\d{3})[ .-]\\d{3}[-. ]\\d{4}", Pattern.CASE_INSENSITIVE);
			Matcher regexMatcherphone = patphone.matcher(Text);
			
			
			
			while(regexMatcherphone.find())
			{
				
				phones.add(regexMatcherphone.group());
			}
			findLinks(doc);
			if(contactusurl.size()==0)
			{
				int index = Text.toLowerCase().indexOf("contact");
				if(index == -1)
					return;
				String str1 = Text.substring(index);
				final Pattern paturl = Pattern.compile("(https?|ftp|file|#)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", Pattern.CASE_INSENSITIVE);
				Matcher regexMatcherUrl = paturl.matcher(str1);
				while(regexMatcherUrl.find())
				{
					
					String str=regexMatcherUrl.group();
					//System.out.println(str);
					//String tok[] = str.split("\\.");
					if(str.toLowerCase().contains("contact") ||  str.toLowerCase().contains("about"))
						contactusurl.add(str);
				}
			}
			//System.out.println("size"+contactusurl.size());
			/*final Pattern paturl = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", Pattern.CASE_INSENSITIVE);
			Matcher regexMatcherUrl = paturl.matcher(Text);
			while(regexMatcherUrl.find())
			{
				
				String str=regexMatcherUrl.group();
				System.out.println(str);
				//String tok[] = str.split("\\.");
				if(str.contains("contact") )
					contactusurl.add(str);
			}*/
			/*Elements e=doc.getElementsByAttribute("href");
			for(Element elem : e)
			{
				System.out.println(elem.);
				//contactusurl.add(e.val());
				if(e.text().contains("contact"))
				{
					
				}
			
			}*/
			/*
			Elements link = doc.select("a[href]");
			for(Element e : link)
			{
				System.out.println(e.);
				contactusurl.add(e.val());
				if(e.text().contains("contact"))
				{
					
				}
			}*/
			//doc.getElementsContainingText()
		}
		
		catch(SocketTimeoutException e)
		{
			try {
				brlog.write("name : "+ businessname + "url:" + homeurl +"\n");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			try {
				brlog.write("name : "+ businessname + "url:" + homeurl+"\n");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	private void findLinks(Document doc) 
	{
		//System.out.println("Text: "+doc.text());
		Elements links = doc.select("[href]");
		//System.out.println("size: "+links.size());
        for (Element link : links) 
        {
        	String text = link.attr("abs:href");
            if(text.toLowerCase().contains("contact") || text.toLowerCase().contains("about"))
				contactusurl.add(text);
        }
		

        /*Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");
*/
        
/*        for (Element src : media) 
        {
        	String text = src.attr("abs:src");
        	if(text.toLowerCase().contains("contact"))
        	{
        		String Text = src.text();
        		final Pattern paturl = Pattern.compile("(https?|ftp|file|#)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", Pattern.CASE_INSENSITIVE);
    			Matcher regexMatcherUrl = paturl.matcher(Text);
    			while(regexMatcherUrl.find())
    			{
    				
    				String str=regexMatcherUrl.group();
    				//System.out.println(str);
    				
    				if(str.toLowerCase().contains("contact") )
    					contactusurl.add(str);
    			}
        	}
        }

        
        for (Element link : imports) 
        {
            String text = link.attr("abs:href");
            if(text.toLowerCase().contains("contact") )
				contactusurl.add(text);
        }
*/
        
	}

	public void tryFindingURL() 
	{
		 
		
	}

	public void crawlcontactusurl() 
	{
		for (String curl : contactusurl) 
		{
			Document doc;
			String url = curl;
			//System.out.println("url: "+url);
			Connection con  = Jsoup.connect(url);
			
			try
			{		
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
					
					emails.add(regexMatcher.group());
				}
				if(!emails.isEmpty())
				{
					emailid_present=true;
				}
			}
			catch (IOException e) 
			{
				
				e.printStackTrace();
			}
		}
		
	}
}
