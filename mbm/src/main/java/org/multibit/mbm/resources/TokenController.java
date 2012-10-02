package org.multibit.mbm.resources;

import org.multibit.mbm.util.CookieUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

// TODO Consider if this is required and refactor to resource
@Controller
@RequestMapping(value = "/v1")
public class TokenController {
  
  public static final String TOKEN_COOKIE_NAME="mbm-token";

  /**
   * <p>Some RESTful endpoints require access to a persistent entity to locate the resource. This token provides
   * the necessary consistency without requiring the customer to log in when they just want to browse around a
   * catalog picking items. Once they are ready to pay, then full authentication kicks in.</p>
   * @param request The original request (injected)
   * @param response The original response (injected)
   * @return A token and a cookie
   */
  @RequestMapping(value = "/token", method = RequestMethod.GET)
  @ResponseBody
  public String getToken(HttpServletRequest request, HttpServletResponse response) {

    String uuid = CookieUtils.getCookieValue(request,TOKEN_COOKIE_NAME);
    if (uuid==null) {
      // Need to generate one
      uuid = UUID.randomUUID().toString();
      Cookie cookie = CookieUtils.CookieBuilder.newInstance()
        .withName(TOKEN_COOKIE_NAME)
        .withValue(uuid)
        .build();
      response.addCookie(cookie);
    }
    
    return uuid; 
  }

}

