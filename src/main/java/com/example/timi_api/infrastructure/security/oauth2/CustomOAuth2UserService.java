package com.example.timi_api.infrastructure.security.oauth2;

import com.example.timi_api.domain.constant.Role;
import com.example.timi_api.domain.entity.Account;
import com.example.timi_api.infrastructure.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Account account = accountRepository.findByEmail(email)
                .orElseGet(() -> {
                    Account newAccount = new Account();
                    newAccount.setEmail(email);
                    newAccount.setFullName(name);
                    newAccount.setPasswordHash("");
                    newAccount.setRole(Role.USER);
                    return accountRepository.save(newAccount);
                });

        return oAuth2User;
    }
}
