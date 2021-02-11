package com.github.trionfettinicoUNICAM.PPTeam_DOIT.service;

import com.github.trionfettinicoUNICAM.PPTeam_DOIT.exception.EntityNotFoundException;
import com.github.trionfettinicoUNICAM.PPTeam_DOIT.exception.IdConflictException;
import com.github.trionfettinicoUNICAM.PPTeam_DOIT.model.*;
import com.github.trionfettinicoUNICAM.PPTeam_DOIT.repository.OrganizationRepository;
import com.github.trionfettinicoUNICAM.PPTeam_DOIT.repository.ProjectRepository;
import com.github.trionfettinicoUNICAM.PPTeam_DOIT.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
// TODO: 22/01/2021 controllare annotazioni perche forse service non e un singleton
public class SimpleOrganizationsManager implements OrganizationsManager{

    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public Organization getInstance(String organizationId) throws EntityNotFoundException {
        if(organizationId.isBlank()) throw new IllegalArgumentException("Il campo 'ID' è vuoto");
        return organizationRepository.findById(organizationId).orElseThrow(()->
                new EntityNotFoundException("Nessuna organizzazione trovata con l'ID: "+organizationId));
    }

    @Override
    public Organization create(Organization organization) throws EntityNotFoundException, IdConflictException {
        if(!userRepository.existsById(organization.getCreatorMail()))
            throw new EntityNotFoundException("Nessun utente trovato con la mail: "+organization.getCreatorMail());
        for (String memberMail : organization.getMembersMails()) {
            if (!userRepository.existsById(memberMail))
                throw new EntityNotFoundException("Nessun utente con la mail: "+memberMail);
        }
        if(existsName(organization.getName()))
            throw new IdConflictException("Esiste gia un organizzazione con questo nome");
        if(!organization.getMembersMails().contains(organization.getCreatorMail()))
            organization.addMember(organization.getCreatorMail());
        return organizationRepository.save(organization);
    }

    @Override
    public boolean delete(String organizationId) {
        if(organizationId.isBlank()) throw new IllegalArgumentException("Il campo 'ID' è vuoto");
        if(!exists(organizationId)) return false;
        organizationRepository.deleteById(organizationId);
        for (Project project: projectRepository.findByOrganizationId(organizationId)) {
            projectRepository.deleteById(project.getId());
        }
        return !exists(organizationId);
    }

    @Override
    public Organization update(Organization organization) throws EntityNotFoundException {
        if(!exists(organization.getId()))
            throw new EntityNotFoundException("Nessuna organizzazione con id: "+ organization.getId());
        return organizationRepository.save(organization);
    }

    @Override
    public List<User> getUsers(String organizationId) throws EntityNotFoundException {
        if(organizationId.isBlank()) throw new IllegalArgumentException("Il campo 'ID' è vuoto");
        Organization org = organizationRepository.findById(organizationId).orElseThrow(()->
                new EntityNotFoundException("Nessuna organizzazione trovata con l'ID: "+organizationId));
        List<User> users = new ArrayList<>();
        for (String email : org.getMembersMails()) {
            users.add(userRepository.findById(email).orElseThrow(()->
                    new EntityNotFoundException("Nessun utente trovato con la mail: "+email)));
        }
        return users;
    }

    @Override
    public boolean existsName(String organizationName) {
        if(organizationName.isBlank()) throw new IllegalArgumentException("Il campo 'name' è vuoto");
        return organizationRepository.findAll().stream()
                .anyMatch(it->it.getName().equals(organizationName));
    }

    @Override
    public boolean exists(String organizationId) {
        if(organizationId.isBlank()) throw new IllegalArgumentException("Il campo 'ID' è vuoto");
        return organizationRepository.existsById(organizationId);
    }

    @Override
    public List<Organization> findByUser(String userMail) {
        if(userMail.isBlank()) throw new IllegalArgumentException("Il campo 'userMail' è vuoto");
        return organizationRepository.findByMember(userMail);
    }

    @Override
    public List<Organization> findByCreator(String userMail) {
        if(userMail.isBlank()) throw new IllegalArgumentException("Il campo 'userMail' è vuoto");
        return organizationRepository.findByCreatorMail(userMail);
    }

    public Page<BasicOrganizationInformation> getPage(int page, int size) throws EntityNotFoundException {
        Page<Organization> projectPage = organizationRepository.findAll(PageRequest.of(page, size));
        List<BasicOrganizationInformation> basicOrganizationInformation = new java.util.ArrayList<>(Collections.emptyList());
        for(Organization organization : projectPage){
            User creator = userRepository.findById(organization.getCreatorMail())
                    .orElseThrow(()->
                            new EntityNotFoundException("Nessun utente trovato con la mail: "+organization.getCreatorMail()));
            basicOrganizationInformation.add(new BasicOrganizationInformation(organization,creator));
        }
        return new PageImpl<>(basicOrganizationInformation);
    }

    @Override
    public void addCollaborator(String organizationId, String userMail, Skill skill) throws EntityNotFoundException {
        if(organizationId.isBlank()) throw new IllegalArgumentException("Il campo 'ID' è vuoto");
        if(userMail.isBlank()) throw new IllegalArgumentException("Il campo 'userMail' è vuoto");
        Organization organization = organizationRepository.findById(organizationId).orElseThrow(()->
                new EntityNotFoundException("Nessun organizzazione trovata con l'ID: "+organizationId));
        User user = userRepository.findById(userMail).orElseThrow(()->
                new EntityNotFoundException("Nessun utente trovato con la mail: "+userMail));
        organization.addMember(userMail);
        organizationRepository.save(organization);
        user.setExpert(skill,organizationId);
        userRepository.save(user);
    }

    @Override
    public boolean removeMember(String organizationId, String memberMail) throws EntityNotFoundException {
        if(organizationId.isBlank()) throw new IllegalArgumentException("Il campo 'ID' è vuoto");
        if(memberMail.isBlank()) throw new IllegalArgumentException("Il campo 'memberMail' è vuoto");
        Organization organization = getInstance(organizationId);
        User user = userRepository.findById(memberMail).orElseThrow(()->
                new EntityNotFoundException("Nessun utente trovato con la mail: "+memberMail));
        if(!organization.getMembersMails().contains(memberMail)) return false;
        organization.removeMember(memberMail);
        user.getSkills().forEach(skill -> skill.getExpertInOrganization().remove(organizationId));
        User savedUser = userRepository.save(user);
        Organization savedOrg = organizationRepository.save(organization);
        return organization.equals(savedOrg) && user.equals(savedUser);
    }

    @Override
    public boolean addMember(String organizationId, String memberMail) throws EntityNotFoundException {
        if(organizationId.isBlank()) throw new IllegalArgumentException("Il campo 'ID' è vuoto");
        if(memberMail.isBlank()) throw new IllegalArgumentException("Il campo 'memberMail' è vuoto");
        Organization organization = getInstance(organizationId);
        if(!userRepository.existsById(memberMail))
            throw new EntityNotFoundException("Nessun utente trovato con la mail: "+memberMail);
        if(organization.getMembersMails().contains(memberMail)) return false;
        organization.addMember(memberMail);
        Organization savedOrg = organizationRepository.save(organization);
        return organization.equals(savedOrg);
    }
}
