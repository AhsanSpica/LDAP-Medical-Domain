package com.test.springldaptest.springldaptest;

import com.test.springldaptest.springldaptest.Model.Group;
import com.test.springldaptest.springldaptest.Model.Person;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.DirContextOperations;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import javax.naming.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.ldap.support.LdapNameBuilder;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import java.util.*;

@Controller
public class HouseResource {
    public static final String BASE_DN = "dc=springframework,dc=org";
    //    @Autowired
//    private LdapTemplate ldapTemplate;
    List <Person> permitted = new ArrayList<Person>();
    List<Group> clinicslist = new ArrayList<Group>();
    List<Group> facilitylist = new ArrayList<Group>();
    List<Group> permissions = new ArrayList<Group>();
    List<Person> persons = new ArrayList<Person>();
    List<Group> groups = new ArrayList<Group>();
    String role="";
    String pUid="";
    String pFull="";
    @Autowired
    private LdapTemplate ldapTemplate;

    private static Logger log = LoggerFactory.getLogger(HouseResource.class);
    //@RequestMapping ( method = RequestMethod.GET)
    //  @RequestMapping(value = "/ex/foos", method = RequestMethod.GET)
    @GetMapping("/newlogin")
    public String login()
    {
        return "newlogin";
    }

    @RequestMapping ("/")
    public String index(Model model)
    {
        String name_page="Test-Page";
        log.info("Getting UsernamePasswordAuthenticationToken from SecurityContextHolder");
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken)
                        SecurityContextHolder.getContext().getAuthentication();

        log.info("Getting principal from UsernamePasswordAuthenticationToken");
        LdapUserDetailsImpl principal = (LdapUserDetailsImpl) authentication.getPrincipal();
        Collection<GrantedAuthority> authorities =  principal.getAuthorities();
        pUid=principal.getUsername();

        log.info("DN details: "+principal.getDn());
        log.info("getAuthorities details: "+String.valueOf(principal.getAuthorities()));

        // Iterator<GrantedAuthority> iterator = authorities.iterator();
        log.info("authentication: " + authentication);
        log.info("principal: " + principal);

        //create orgUnit

//        Name dn = buildOrgUnitDn("facility");
//        ldapTemplate.bind(dn, null, buildOUAttributes("facility"));
//        Name bdn = buildOrgUnitDn("clinic");
//        ldapTemplate.bind(bdn, null, buildOUAttributes("clinic"));
        //end of create orgUnit
        //create clinc and facility method test
//        createfacilitytest("FergusonFacility");
//        createclinictest("FergusonFacility","HarisClinic");
//        createclinictest("FergusonFacility","DavidClinic");
//        createfacilitytest("DemoineFacility");
//        createclinictest("DemoineFacility","JohnHopkins-Clinic");
//        createclinictest("DemoineFacility","ENT-Clinic");
//        log.info(findAllClinics().toString());
        //please check if persons and groups exist
        Person dumpers= new Person();
        dumpers.setFullName("John Doe");
        dumpers.setLastName("Doe");
        dumpers.setUid("dummy");
        Group dumpgr = new Group();
        dumpgr.setName("dummygroup");
        try
        { persons = findAllPersons();} catch (Exception ex){persons.add(dumpers);}
        try
        {groups = findAllGroups();} catch (Exception ex){groups.add(dumpgr);}

        permissions.add(dumpgr);
        List<String> luid= new ArrayList<String>();
        for (Person person : persons)
        {
            luid.add( person.getUid());
            if (person.getUid().equals(pUid))
            {
                pFull =person.getFullName();
            }
        }
      //  log.info("All persons uids: "+luid.toString());

        for (GrantedAuthority grantedAuthority : authorities) {
            role=grantedAuthority.getAuthority();

            if (role.equals("ROLE_ADMIN")) {
                log.info("Granted Authority: "+grantedAuthority.getAuthority());
                model.addAttribute("qname", pFull);
                model.addAttribute("role",role);
                model.addAttribute("Person", persons);
                model.addAttribute("Group", groups);
                model.addAttribute("Permits",permissions);
                //  name_page = "Admin-page";
                name_page ="starteradmin";
            }
            else
            {
                model.addAttribute("qname", pFull);
                model.addAttribute("role",role);
                name_page= "Test-Page";
            }
        }

        return name_page;
    }

    //

    @RequestMapping(value="/add-uid-to-group",params="action=assigngr",method = RequestMethod.POST)
    public String addUidtoGroup(
            @RequestParam(required = false, name = "members") String member,
            @RequestParam(required = false, name = "roles") String group,
            Model model)
        {
        persons = findAllPersons();
        groups = findAllGroups();
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken)
                        SecurityContextHolder.getContext().getAuthentication();

        LdapUserDetailsImpl principal = (LdapUserDetailsImpl) authentication.getPrincipal();
        Name groupDn = buildGroupDn(group.toUpperCase().toString());
        Name personDn = buildPersonDn(member.toUpperCase().toString());
        DirContextOperations ctx = ldapTemplate.lookupContext(groupDn);
        ctx.addAttributeValue("uniqueMember", personDn);
        ldapTemplate.modifyAttributes(ctx);
        log.info("All groups:  "+groups.toString());
        if(!(findAllperms().isEmpty()))
        {
            permissions =findAllperms();
        }
        model.addAttribute("qname", pUid);
        model.addAttribute("role",role);
        model.addAttribute("Person", persons);
        model.addAttribute("Group", groups);
        model.addAttribute("Permits",permissions);
        model.addAttribute("Message",member+", set to: "+group);
        // return "Admin-page";
        return "starteradmin";
    }

    @RequestMapping(value="/add-uid-to-group",params="action=assignpr",method = RequestMethod.POST)
    public String addUidtoPermission(
            @RequestParam(required = false, name = "members") String member,
            @RequestParam(required = false, name = "permits") String group,
            Model model)
    {
        persons = findAllPersons();
        groups = findAllGroups();
        permissions = findAllperms();
        Group grouptemp= new Group();
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken)
                        SecurityContextHolder.getContext().getAuthentication();
        LdapUserDetailsImpl principal = (LdapUserDetailsImpl) authentication.getPrincipal();
        Name groupDn = buildGroupDn(group.toUpperCase().toString());
        Name personDn = buildPersonDn(member.toUpperCase().toString());
        log.info(member+", persondn: "+personDn.toString());
        log.info("AllGroupsOfPerson "+member+" :  "+findAllGroupsOfPerson(personDn).toString());
        //for checking permission according to group
//        List <Group> gro = findAllGroupsOfPerson(personDn);
//         for (Group egro:gro)
//         {
//             if (egro.getName().equals("CC"))
//             {
//
//             }
//         }
        DirContextOperations ctx = ldapTemplate.lookupContext(groupDn);
        ctx.addAttributeValue("uniqueMember", personDn);
        ldapTemplate.modifyAttributes(ctx);
        for (Group gr:groups)
        {
            if (gr.getName().equals(group))
            {
                grouptemp= gr;
            }
        }
        permissions = findAllperms();
        model.addAttribute("qname", pUid);
        model.addAttribute("role",role);
        model.addAttribute("Person", persons);
        model.addAttribute("Group", groups);
        model.addAttribute("Permits", permissions);
        model.addAttribute("Message",member+", set to: "+group);
        log.info("All members of the permission :  "+grouptemp.toString());
        // return "Admin-page";
        return "starteradmin";
    }

    @RequestMapping(value="/create-person",method = RequestMethod.POST)
    public String createPerson(@RequestParam(required = false, name = "puid" ) String puid,
                               @RequestParam(required = false, name = "fname" ) String fname,
                               @RequestParam(required = false, name = "lname" ) String lname,
                               Model model) {
        persons = findAllPersons();
        groups = findAllGroups();
        Person person = new Person(puid, fname, lname);
        Name dn = buildPersonDn(person.getUid());
        ldapTemplate.bind(dn, null, buildPAttributes(person));

        model.addAttribute("qname", pUid);
        model.addAttribute("role",role);
        model.addAttribute("Person", persons);
        model.addAttribute("Group", groups);
        model.addAttribute("Message","New Person created"+person.getUid());

        // return "Admin-page";
        return "starteradmin";
    }

    public void createclinictest(String facilitynm, String clinicnm) {

        Group group = new Group();
        group.setName(clinicnm);
        Name clinicDn = buildClinicDn(clinicnm);
        ldapTemplate.bind(clinicDn, null, buildCAttributes(clinicnm,clinicnm));

        Name facilityDn = buildFacilityDn(facilitynm);
        DirContextOperations ctx = ldapTemplate.lookupContext(clinicDn);
        ctx.addAttributeValue("uniqueMember", facilityDn);
        ldapTemplate.modifyAttributes(ctx);

        ctx = ldapTemplate.lookupContext(facilityDn);
        ctx.addAttributeValue("uniqueMember", clinicDn);
        ldapTemplate.modifyAttributes(ctx);
        log.info("query ou=clinics: "+findAllclinics().toString());
        log.info("query ou=facility: "+findAllfacility().toString());
    }

    public void createfacilitytest(String facilitynm) {

        Group group = new Group();
        group.setName(facilitynm);
        Name facilityDn = buildFacilityDn(facilitynm);
        ldapTemplate.bind(facilityDn, null, buildFAttributes(facilitynm,facilitynm));

        log.info("query ou=facility"+findAllfacility().toString());
        log.info("facility lookup after creating facility  "+ldapTemplate.lookup(facilityDn).toString());
    }

    @RequestMapping(value="/add-uid-to-group",params="action=createcl",method = RequestMethod.POST)
    public String createclinics(@RequestParam(required = false, name = "facilities") String facilities,
                                @RequestParam(required = false, name = "clname") String clname,
                                Model model) {
            Group group = new Group();
            String msg = "";

            if (!(clname.isEmpty()) && (!facilities.equals("Select"))) {
            group.setName(clname);
            Name clinicDn = buildClinicDn(clname);
               // {DirContextOperations ctx = ldapTemplate.lookupContext(clinicDn);}
            ldapTemplate.bind(clinicDn, null, buildCAttributes(clname, clname));

            Name facilityDn = buildFacilityDn(facilities);
            DirContextOperations ctx = ldapTemplate.lookupContext(clinicDn);
            ctx.addAttributeValue("uniqueMember", facilityDn);
            ldapTemplate.modifyAttributes(ctx);

            ctx = ldapTemplate.lookupContext(facilityDn);
            ctx.addAttributeValue("uniqueMember", clinicDn);
            ldapTemplate.modifyAttributes(ctx);
            log.info("query ou=clinics: " + findAllclinics().toString());
            log.info("query ou=facility: " + findAllfacility().toString());
                msg="New Clinic created: " + group.getName();
            }
            else{msg="You left either Facility or Clinic Empty";}
            boolean bool = false;
            persons = findAllPersons();
            groups = findAllGroups();
            permissions = findAllperms();
            clinicslist = findAllclinics();
            facilitylist = findAllfacility();

            model.addAttribute("qname", pUid);
            model.addAttribute("role", role);
            model.addAttribute("Person", persons);
            model.addAttribute("Group", groups);
            model.addAttribute("Permits", permissions);
            model.addAttribute("Clinic", clinicslist);
            model.addAttribute("Facility", facilitylist);
            model.addAttribute("Message", msg);

        return "starteradmin";

    }

    @RequestMapping(value="/add-uid-to-group",params="action=createfc",method = RequestMethod.POST)
    public String createfacility(@RequestParam(required = false, name = "fcname") String fcname,Model model) {

               Group group = new Group();
               String msg = "";
                   if (!(fcname.isEmpty())) {
                       try {
               group.setName(fcname);
               Name facilityDn = buildFacilityDn(fcname);
               ldapTemplate.bind(facilityDn, null, buildFAttributes(fcname, fcname));
               log.info("query ou=facility" + findAllfacility().toString());
               log.info("facility lookup after creating facility  " + ldapTemplate.lookup(facilityDn).toString());
               boolean bool = false;
               msg = "New Facility created: " + fcname;
       }
           catch(Exception ex )
        {
            log.info(ex.getClass().toString());
            if (ex.getClass().equals(org.springframework.ldap.NameAlreadyBoundException.class))
            {
                msg="Facility with name "+fcname+" already created.";
            }
        }
       }
           persons = findAllPersons();
           groups = findAllGroups();
           facilitylist = findAllfacility();
           model.addAttribute("qname", pUid);
           model.addAttribute("role", role);
           model.addAttribute("Person", persons);
           model.addAttribute("Group", groups);
           model.addAttribute("Permits", permissions);
           model.addAttribute("Facility", facilitylist);

           //model.addAttribute("clinics",clinicslist);
           model.addAttribute("Message", msg);

        // return "Admin-page";
        return "starteradmin";
    }

    @RequestMapping(value="/add-uid-to-group",params="action=creategr",method = RequestMethod.POST)
    public String createGroup(@RequestParam(required = false, name = "grname") String groupnm, Model model) {

        Group group = new Group();
        group.setName(groupnm);
        Name dn = buildGroupDn(group.getName());
        ldapTemplate.bind(dn, null, buildGAttributes(group,"groups"));
        boolean bool =false;

        for(Group perm: permissions)
        { if (perm.getName().equals("dummygroup"))
        {
            bool = true;
        }
        }
        persons = findAllPersons();
        groups = findAllGroups();
        if (bool)
        {
            permissions.set(0,group);
        }
        else {
            permissions.add(group);
        }
        //log.info("permission list :  "+permissions.toString());
        log.info("query ou=subgroups"+findAllperms().toString());
        model.addAttribute("qname", pUid);
        model.addAttribute("role",role);
        model.addAttribute("Person", persons);
        model.addAttribute("Group", groups);
        model.addAttribute("Permits",permissions);
        model.addAttribute("Message","New Group created: "+group.getName());

        // return "Admin-page";
        return "starteradmin";
    }

    @RequestMapping(value="/add-uid-to-group",params="action=createpr",method = RequestMethod.POST)
    public String createPermission(@RequestParam(required = false, name = "prname") String groupnm, Model model) {

       if(!(groupnm.isEmpty())) {
           Group group = new Group();
           group.setName(groupnm);
           Name dn = buildGroupDn(group.getName());
           ldapTemplate.bind(dn, null, buildGAttributes(group, "subgroups"));

           boolean bool = false;
           for (Group perm : permissions) {
               if (perm.getName().equals("dummygroup")) {
                   bool = true;
               }
           }
           if (bool) {
               permissions.set(0, group);
           } else {
               permissions.add(group);
           }

           persons = findAllPersons();
           groups = findAllGroups();

           //log.info("permission list :  "+permissions.toString());
           log.info("query ou=subgroups" + findAllperms().toString());
           model.addAttribute("qname", pUid);
           model.addAttribute("role", role);
           model.addAttribute("Person", persons);
           model.addAttribute("Group", groups);
           model.addAttribute("Permits", permissions);
           model.addAttribute("Message", "New Permission created: " + group.getName());
       }
        // return "Admin-page";
        return "starteradmin";
    }
    @RequestMapping(value="/getclinics",method = RequestMethod.GET)
    public String getallclinics(Model model) {
        persons = findAllPersons();
        groups = findAllGroups();
        permissions = findAllperms();
        facilitylist = findAllfacility();
        clinicslist = findAllclinics();
        List<Group> cangs = findAllclinics();
        model.addAttribute("qname", pUid);
        model.addAttribute("role",role);
        model.addAttribute("Person", persons);
        model.addAttribute("Group", groups);
        model.addAttribute("Permits",permissions);
        model.addAttribute("Facility",facilitylist);
        model.addAttribute("Clinic",clinicslist);

        model.addAttribute("cangs",cangs);

        // return "Admin-page";
        return "starteradmin";
    }

    @RequestMapping(value="/getfacility",method = RequestMethod.GET)
    public String getallfacility(Model model) {
        persons = findAllPersons();
        groups = findAllGroups();
        permissions = findAllperms();
        facilitylist = findAllfacility();


        List<Group> fangs = findAllfacility();
        model.addAttribute("qname", pUid);
        model.addAttribute("role",role);
        model.addAttribute("Person", persons);
        model.addAttribute("Group", groups);
        model.addAttribute("Permits",permissions);
        model.addAttribute("Facility",facilitylist);
        model.addAttribute("Clinic",clinicslist);
        model.addAttribute("fangs",fangs);

        // return "Admin-page";
        return "starteradmin";
    }

    @RequestMapping(value="/getgroups",method = RequestMethod.GET)
    public String getallgroups(Model model) {
        persons = findAllPersons();
        groups = findAllGroups();
        facilitylist = findAllfacility();
        clinicslist = findAllclinics();
        List<Group> gangs = findAllGroups();
   //     List <String> grnm = new ArrayList<>();
//        for(Group grp:gangs)
//        {
//            grnm.add(grp.getName());
//        }

        model.addAttribute("qname", pUid);
        model.addAttribute("role",role);
        model.addAttribute("Person", persons);
        model.addAttribute("Group", groups);
        model.addAttribute("Permits",permissions);
        model.addAttribute("Clinic",clinicslist);
        model.addAttribute("Facility",facilitylist);
        model.addAttribute("gangs",gangs);

        // return "Admin-page";
        return "starteradmin";
    }

    @RequestMapping(value="/getallperms",method = RequestMethod.GET)
    public String getallperms(Model model) {
        persons = findAllPersons();
        groups = findAllGroups();
        permissions = findAllperms();
        facilitylist = findAllfacility();
        clinicslist = findAllclinics();
        //     List <String> grnm = new ArrayList<>();
//        for(Group grp:gangs)
//        {
//            grnm.add(grp.getName());
//        }

        model.addAttribute("qname", pUid);
        model.addAttribute("role",role);
        model.addAttribute("Person", persons);
        model.addAttribute("Group", groups);
        model.addAttribute("Permits",permissions);
        model.addAttribute("Clinic",clinicslist);
        model.addAttribute("Facility",facilitylist);
        model.addAttribute("pangs",permissions);

        // return "Admin-page";
        return "starteradmin";
    }

    @RequestMapping(value="/add-to-CF")
    public String addtoCF(Model model) {
        persons = findAllPersons();
        groups = findAllGroups();

        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken)
                        SecurityContextHolder.getContext().getAuthentication();

        LdapUserDetailsImpl principal = (LdapUserDetailsImpl) authentication.getPrincipal();
        String puid=principal.getUsername();
        Name groupDn = buildGroupDn("CF");
        Name personDn = buildPersonDn(puid);

        DirContextOperations ctx = ldapTemplate.lookupContext(groupDn);
        ctx.addAttributeValue("uniqueMember", personDn);
        ldapTemplate.modifyAttributes(ctx);
        log.info("All groups:  "+groups.toString());

        model.addAttribute("qname", pUid);
        model.addAttribute("role",role);
        model.addAttribute("Person", persons);
        model.addAttribute("Group", groups);
        model.addAttribute("Permits",permissions);
        model.addAttribute("Message",pUid+", is set to CF");
        // return "Admin-page";
        return "starteradmin";
    }
    @RequestMapping(value="/add-to-CM")
    public String addtoCM(Model model) {

        persons = findAllPersons();
        groups = findAllGroups();

        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken)
                        SecurityContextHolder.getContext().getAuthentication();

        LdapUserDetailsImpl principal = (LdapUserDetailsImpl) authentication.getPrincipal();
        String puid=principal.getUsername();
        Name groupDn = buildGroupDn("CM");
        Name personDn = buildPersonDn(puid);

        DirContextOperations ctx = ldapTemplate.lookupContext(groupDn);
        ctx.addAttributeValue("uniqueMember", personDn);
        ldapTemplate.modifyAttributes(ctx);
        log.info("All groups:  "+groups.toString());

        model.addAttribute("qname", pUid);
        model.addAttribute("role",role);
        model.addAttribute("Message",pUid+", is set to CM");
        model.addAttribute("Person", persons);
        model.addAttribute("Group", groups);
        model.addAttribute("Permits",permissions);

        // return "Admin-page";
        return "starteradmin";
    }
    @RequestMapping(value="/add-to-CC")
    public String addtoCC(Model model) {
        persons = findAllPersons();
        groups = findAllGroups();

        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken)
                        SecurityContextHolder.getContext().getAuthentication();

        LdapUserDetailsImpl principal = (LdapUserDetailsImpl) authentication.getPrincipal();
        String puid=principal.getUsername();
        Name groupDn = buildGroupDn("CC");
        Name personDn = buildPersonDn(puid);

        DirContextOperations ctx = ldapTemplate.lookupContext(groupDn);
        ctx.addAttributeValue("uniqueMember", personDn);
        ldapTemplate.modifyAttributes(ctx);
        log.info("All groups:  "+groups.toString());

        model.addAttribute("qname", pUid);
        model.addAttribute("role",role);
        model.addAttribute("Person", persons);
        model.addAttribute("Group", groups);
        model.addAttribute("Permits",permissions);
        model.addAttribute("Message",pUid+", is set to CC");
        //return "Admin-page";
        return "starteradmin";
    }
//    @RequestMapping("/member/{id}")
//    public String developer(@PathVariable Long id, Model model) {
//
//        model.addAttribute("Member", repository.findOne(id));
//        model.addAttribute("Roles", skillRepository.findAll());
//        return "member";
//    }



    //step 2.a Get Method display UID and roles in Json
//    @RequestMapping(value="roleassign", method = RequestMethod.GET)
//    public Response roleassingone ( )
//    {
//
//        return null;
//    }
//    //step 2.b Get Method display Roles and Methods
//    @RequestMapping(value = "roleassign", method=RequestMethod.GET)
//    public Response roleassingtwo ()
//    {
//
//        return null;
//    }
//    //step 3
//    @RequestMapping(value = "roleassign", method=RequestMethod.POST)
//    public Response roleassingthree (@RequestParam String uid,@RequestParam String role)
//    {
//       //if role permission check if request role is allowed the privelege
//        return null;
//    }
    public List<Person> findAllPersons() {
        EqualsFilter filter = new EqualsFilter("objectclass", "person");
        return ldapTemplate.search(LdapUtils.emptyLdapName(), filter.encode(), new PersonContextMapper());
    }
    public List<Group> findAllperms(){
        List<Group> gr = new ArrayList<Group>();
        try {
            gr = ldapTemplate.search( query().where("objectclass").is("groupOfUniqueNames")
                            .and("ou").is("subgroups")
                    , new GroupContextMapper());
        }
        catch(Exception ex){
            log.info(ex.getCause()+" "+ex.getMessage());
            return gr;
        }
        return gr;
    }
    public List<Group> findAllGroups(){
        Name basedn = buildOrgUnitDn("groups");

        List<Group> gr = new ArrayList<Group>();
        try {
            gr = ldapTemplate.search( query().base(basedn).where("objectclass").is("groupOfUniqueNames")
                    //   .and("dn").is("ou=clinic,dc=springframework,dc=org")
                    // .and("ou").is("clinic")
                            .and("ou").not().is("subgroups")
                    , new GroupContextMapper());
        }
        catch(Exception ex){
            log.info(ex.getCause()+" "+ex.getMessage());
            return gr;
        }
        return gr;

    }
    public List<Group> findAllGroupsOfPerson(Name person) {
        List<Group> gr = new ArrayList<Group>();
        try {
            gr = ldapTemplate.search( query().where("objectclass").is("groupOfUniqueNames")
                            .and("uniqueMember").is(person.toString())
                    , new GroupContextMapper());
        }
        catch(Exception ex){
            log.info(ex.getCause()+" "+ex.getMessage());
            return gr;
        }
        return gr;
    }
    public List<Group> findAllclinics(){

        Name basedn = buildOrgUnitDn("clinic");
        List<Group> gr = new ArrayList<Group>();
        try {
            gr = ldapTemplate.search( query().base(basedn).where("objectclass").is("groupOfUniqueNames")
                         //   .and("dn").is("ou=clinic,dc=springframework,dc=org")
                    // .and("ou").is("clinic")
                    , new GroupContextMapper());
        }
        catch(Exception ex){
            log.info(ex.getCause()+" "+ex.getMessage());
            return gr;
        }
        return gr;
    }
    public List<Group> findAllfacility(){

        Name basedn = buildOrgUnitDn("facility");
        List<Group> gr = new ArrayList<Group>();
        try {
            gr = ldapTemplate.search( query().base(basedn).where("objectclass").is("groupOfUniqueNames")
                        //    .and("dn").is("ou=facility,dc=springframework,dc=org")
                    //.and("ou").is("facility")
                    , new GroupContextMapper());
        }
        catch(Exception ex){
            log.info(ex.getCause()+" "+ex.getMessage());
            return gr;
        }
        return gr;
    }

    public Attributes buildPAttributes(Person p) {
        Attributes attrs = new BasicAttributes();
        BasicAttribute ocAttr = new BasicAttribute("objectclass");
        ocAttr.add("top");
        ocAttr.add("person");
        ocAttr.add("organizationalPerson");
        attrs.put(ocAttr);
        attrs.put("ou", "people");
        attrs.put("uid", p.getUid());
        attrs.put("cn", p.getFullName());
        attrs.put("sn", p.getLastName());
        return attrs;
    }
    public Attributes buildGAttributes(Group g, String perm) {

        Attributes attrs = new BasicAttributes();
        BasicAttribute ocAttr = new BasicAttribute("objectclass");
        ocAttr.add("top");
        ocAttr.add("groupOfUniqueNames");

        attrs.put(ocAttr);
        attrs.put("ou", perm);
        attrs.put("cn", g.getName());
        attrs.put("uniqueMember", buildPersonDn("ben").toString() );
        return attrs;
    }

    public Attributes buildCAttributes(String clinicfnm, String clinicid) {

        Attributes attrs = new BasicAttributes();
        BasicAttribute ocAttr = new BasicAttribute("objectclass");
        ocAttr.add("top");
        ocAttr.add("groupOfUniqueNames");

        attrs.put(ocAttr);
        attrs.put("ou", clinicid);
        attrs.put("cn", clinicfnm);
        attrs.put("uniqueMember", buildFacilityDn("base").toString() );
        return attrs;
    }
    public Attributes buildFAttributes(String facilityfnm, String facilityid) {

        Attributes attrs = new BasicAttributes();
        BasicAttribute ocAttr = new BasicAttribute("objectclass");
        ocAttr.add("top");
        ocAttr.add("groupOfUniqueNames");

        attrs.put(ocAttr);
        attrs.put("ou", facilityid);
        attrs.put("cn", facilityfnm);
        attrs.put("uniqueMember", buildClinicDn("base").toString() );
        return attrs;
    }

    public Attributes buildOUAttributes(String orguid) {

        Attributes attrs = new BasicAttributes();
        BasicAttribute ocAttr = new BasicAttribute("objectclass");
        ocAttr.add("top");
        ocAttr.add("organizationalUnit");
        attrs.put(ocAttr);
        attrs.put("ou", orguid);
        //attrs.put("ou", "subgroups");
        // attrs.put("cn", orgnm);
        return attrs;
    }
    public Name buildOrgUnitDn(String rootnm) {
        return LdapNameBuilder.newInstance(BASE_DN)
                .add("ou", rootnm)
                .build();
    }
    public Name buildPersonDn(String Puid) {
        return LdapNameBuilder.newInstance(BASE_DN)
                .add("ou", "people")
                .add("uid", Puid)
                .build();
    }
    public Name buildClinicDn(String groupName) {
        return LdapNameBuilder.newInstance(BASE_DN)
                .add("ou", "clinic")
                .add("cn", groupName)
                .build();
    }
    public Name buildFacilityDn(String groupName) {
        return LdapNameBuilder.newInstance(BASE_DN)
                .add("ou", "facility")
                .add("cn", groupName)
                .build();
    }

    public Name buildGroupDn(String groupName) {
        return LdapNameBuilder.newInstance(BASE_DN)
                .add("ou", "groups")
                .add("cn", groupName)
                .build();
    }
    public Name buildPermDn(String groupName) {
        return LdapNameBuilder.newInstance(BASE_DN)
                .add("ou", "subgroups")
                .add("cn", groupName)
                .build();
    }
    public static class PersonContextMapper extends AbstractContextMapper<Person> {
        public Person doMapFromContext(DirContextOperations context) {
            Person person = new Person();
            person.setFullName(context.getStringAttribute("cn"));
            person.setLastName(context.getStringAttribute("sn"));
            person.setUid(context.getStringAttribute("uid"));
            return person;
        }
    }
    public static class GroupContextMapper extends AbstractContextMapper<Group> {
        public Group doMapFromContext(DirContextOperations context) {
            Group group = new Group();
            group.setName(context.getStringAttribute("cn"));
            Object[] members = context.getObjectAttributes("uniqueMember");
            for (Object member : members){
                Name memberDn = LdapNameBuilder.newInstance(String.valueOf(member)).build();
                group.addMember(memberDn);
            }
            return group;
        }}

    @GetMapping("/accessDenied")
    public String error(Model model) {
        model.addAttribute("message", "You are not authorized to access this page.");
        return "access-denied";
    }
    @GetMapping("/logout")
    public String logout(Model model) {

        return "redirect:/login";
    }


    // @GetMapping("/hello")
    //   public String hello(@RequestParam(value="name",defaultValue = "World", required = true) String name, Model model)
    //  {
    //     model.addAttribute("name", name);
    //    return "First page";
    // }

//    @RequestMapping(value = "/", method = RequestMethod.POST)
//    public ResponseEntity<?> handleForm(@RequestParam("firstName") String firstName,
//                                        @RequestParam("firstName") String lastName, @RequestParam("role")
//                                                Role role) {
//        logger.info("first Name : {}", firstName);
//        logger.info("Last Name : {}", lastName);
//        logger.info("Role: {}", role);
//        return ResponseEntity.ok().body(firstName);
//    }
}