/* ********************************************************************
*
*  Copyright (c) 2025 Statusneo.
*
* All rights Reserved.
*
* Redistribution and use of any source or binary or in any form, without
* written approval and permission is prohibited.
*
* Please read the Terms of use, Disclaimer and Privacy Policy on https://www.statusneo.com/
*
* **********************************************************************
*/
package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;

public interface EmailService {
    /**
     * Sends an email using the provided Email object.
     *
     * @param email Email object containing all necessary fields
     * @return true if the email was sent successfully, false otherwise
     */
    boolean sendEmail(Email email);
}
