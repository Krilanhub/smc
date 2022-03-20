package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.objenesis.instantiator.basic.NewInstanceInstantiator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME" + userName);

		// get the user username(Email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER" + user);
		model.addAttribute("user", user);
	}

	// Home dashboard
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String OpenAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {

			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// processing and uploading file..

			if (file.isEmpty()) {

				// if the file is empty then try our message
				System.out.println("File is empty");
				contact.setImage("contact.png");

			} else {
				// save the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("/static/img").getFile();

				Path path = Paths.get("D:\\projectdoc" + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded Successfully");

			}

			user.getContacts().add(contact);

			contact.setUser(user);

			this.userRepository.save(user);

			System.out.println("DATA" + contact);
			System.out.println("Added to database Successfully!!");

			// message success...
			session.setAttribute("message", new Message("Your contact is added !! Add More..", "success"));

		} catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();

			// message error
			session.setAttribute("message", new Message("Something went wrong !! Try again..", "danger"));
		}

		return "normal/add_contact_form";
	}

	// show contacts handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable(value = "page", required = false) Integer page, Model m,
			Principal principal, String filter) {
		m.addAttribute("title", "Show User Contacts");

		// get contact list
		
		if(filter==null) {
			filter="";
		}
		

		if (page == null) {
			page = 0;
		}

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		// List<Contact> contacts = user.getContacts();

		// currentPage-page
		// Contact per page - 5
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = null;

		if (filter != null && !filter.trim().equals("")) {

			contacts = this.contactRepository.findContactsByUserWithParams(user.getId(), filter, pageable);
		} else {

			contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		}

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("filter", filter);

		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";

	}

	// showing particular contact details.

	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid, Model model, Principal principal) {
		System.out.println("CID" + cid);

		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();

		//
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}

	// delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model, HttpSession session,
			Principal principal) {

		Contact contact = this.contactRepository.findById(cid).get();

		System.out.println("Contact" + contact.getCid());

		User user = this.userRepository.getUserByUserName(principal.getName());

		user.getContacts().remove(contact);

		this.userRepository.save(user);

		// Delete contact Profile Image

		File deleteFile = new File("D:\\projectdoc" + File.separator + contact.getImage());

		deleteFile.delete();

		System.out.println("DELETED");

		session.setAttribute("message", new Message("Contact deleted Successfully...", "success"));

		return "redirect:/user/show-contacts/0";
	}

	// open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model) {
		model.addAttribute("title", "Update Contact");

		Contact contact = this.contactRepository.findById(cid).get();

		model.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session, Principal principal) {
		try {

			// old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getCid()).get();

			// image...
			if (!file.isEmpty()) {

				// file work..
				// rewrite

				// delete old photo

				File deleteFile = new ClassPathResource("/static/img").getFile();

				File file1 = new File(deleteFile, oldContactDetail.getImage());

				file1.delete();

				// update new photo

				File saveFile = new ClassPathResource("/static/img").getFile();

				Path path = Paths.get("D:\\projectdoc" + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			} else {
				contact.setImage(oldContactDetail.getImage());
			}

			User user = this.userRepository.getUserByUserName(principal.getName());

			contact.setUser(user);
			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your contact is updated...", "success"));

		} catch (Exception e) {

			e.printStackTrace();
			// TODO: handle exception
		}
		return "redirect:/user/" + contact.getCid() + "/contact";
	}

	// your profile handler
	@GetMapping("/profile")
	public String yourprofile(Model model) {
		model.addAttribute("title", "Profile Page");
		return ("normal/profile");

	}

}
