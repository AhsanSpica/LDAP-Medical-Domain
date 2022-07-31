package com.test.springldaptest.springldaptest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.test.springldaptest.springldaptest.*;
import org.springframework.ldap.core.LdapTemplate;

import javax.naming.Name;

@SpringBootApplication
public class SpringLdapTestApplication implements CommandLineRunner {
	@Autowired
	private LdapTemplate ldapTemplate;
	private static Logger log = LoggerFactory.getLogger(HouseResource.class);
	public static void main(String[] args)  {
		SpringApplication.run(SpringLdapTestApplication.class, args);
	}

	public void run(String... args)
	{
		HouseResource h = new HouseResource();
		Name dn = h.buildOrgUnitDn("facility");
		ldapTemplate.bind(dn, null, h.buildOUAttributes("facility"));
		log.info("lookup after creating facility basedomain:  "+ldapTemplate.lookup(dn).toString());
		Name bdn = h.buildOrgUnitDn("clinic");
		ldapTemplate.bind(bdn, null, h.buildOUAttributes("clinic"));
		log.info(" lookup after creating clinic basedomain: "+ldapTemplate.lookup(bdn).toString());

//		Name gdn = h.buildOrgUnitDn("people");
//		ldapTemplate.bind(gdn, null, h.buildOUAttributes("people"));
//		log.info("lookup after creating people basedomain:  "+ldapTemplate.lookup(dn).toString());
//		Name personDn = h.buildPersonDn("ahsan");


	}


}
