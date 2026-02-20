package com.notificationService.utils;

public class EmailTemplateBuilder {

    private static final String BRAND_COLOR = "#0056b3";
    private static final String BG_COLOR = "#f4f7f6";

    // Base wrapper to keep the design consistent across all emails
    private static String getBaseTemplate(String title, String content) {
        return """
            <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: %s; padding: 40px 0; margin: 0;">
                <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); overflow: hidden;">
                    <tr>
                        <td style="background-color: %s; padding: 30px; text-align: center;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 24px; letter-spacing: 1px;">University IT Portal</h1>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 40px 30px; color: #333333; line-height: 1.6; font-size: 16px;">
                            <h2 style="color: #2c3e50; margin-top: 0;">%s</h2>
                            %s
                        </td>
                    </tr>
                    <tr>
                        <td style="background-color: #f9fafb; padding: 20px 30px; text-align: center; color: #888888; font-size: 12px; border-top: 1px solid #eeeeee;">
                            <p style="margin: 0;">&copy; 2026 University IT Department. All rights reserved.</p>
                            <p style="margin: 5px 0 0 0;">This is an automated message, please do not reply.</p>
                        </td>
                    </tr>
                </table>
            </div>
            """.formatted(BG_COLOR, BRAND_COLOR, title, content);
    }

    // 1. OTP Email Template
    public static String buildOtpEmail(String title, String contextMessage, String otp) {
        String content = """
            <p>%s</p>
            <div style="text-align: center; margin: 30px 0;">
                <span style="display: inline-block; font-size: 32px; font-weight: bold; color: %s; background-color: #f0f7ff; padding: 15px 30px; border-radius: 6px; letter-spacing: 4px; border: 1px dashed %s;">
                    %s
                </span>
            </div>
            <p style="font-size: 14px; color: #666;">This code will expire in 5 minutes. If you did not request this, please ignore this email or contact the IT support desk.</p>
            """.formatted(contextMessage, BRAND_COLOR, BRAND_COLOR, otp);
        return getBaseTemplate(title, content);
    }

    // 2. Student Registration Welcome Template
    public static String buildWelcomePendingEmail(String enrollmentNo) {
        String content = """
            <p>Hi <strong>%s</strong>,</p>
            <p>Welcome to the University Platform! Your registration was successful.</p>
            <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; color: #856404; border-radius: 4px;">
                <strong>Status: Pending Approval</strong><br>
                Your account has been forwarded to your Head of Department (HOD) for verification. You will receive another email once your access is approved.
            </div>
            <p>Thank you for your patience.</p>
            """.formatted(enrollmentNo);
        return getBaseTemplate("Registration Successful", content);
    }

    // 3. HOD Action Required Template
    public static String buildHodActionRequiredEmail(String studentEmail, String enrollmentNo) {
        String content = """
            <p>Hello,</p>
            <p>A new student has registered on the portal and requires your approval to access departmental resources.</p>
            <table width="100%%" style="margin: 20px 0; border-collapse: collapse;">
                <tr><td style="padding: 8px; border: 1px solid #ddd; background: #f9f9f9; font-weight: bold; width: 30%%;">Enrollment No</td><td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
                <tr><td style="padding: 8px; border: 1px solid #ddd; background: #f9f9f9; font-weight: bold;">Email</td><td style="padding: 8px; border: 1px solid #ddd;">%s</td></tr>
            </table>
            <p>Please log in to the Admin Dashboard to review and approve this request.</p>
            <div style="text-align: center; margin: 30px 0;">
                <a href="http://localhost:3000/admin/dashboard" style="background-color: %s; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">Go to Dashboard</a>
            </div>
            """.formatted(enrollmentNo, studentEmail, BRAND_COLOR);
        return getBaseTemplate("Action Required: Student Approval", content);
    }

    // 4. Account Approved Template
    public static String buildAccountApprovedEmail() {
        String content = """
            <p>Great news!</p>
            <p>Your account has been officially <strong>approved</strong> by your department. You now have full access to the University Platform.</p>
            <div style="text-align: center; margin: 30px 0;">
                <a href="http://localhost:3000/login" style="background-color: #28a745; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">Log In Now</a>
            </div>
            <p>If you have any questions, feel free to reach out to the IT support team.</p>
            """;
        return getBaseTemplate("Account Approved!", content);
    }

    // 5. Security Alert (Password Reset)
    public static String buildSecurityAlertEmail() {
        String content = """
            <p>Hello,</p>
            <p>This is a confirmation that the password for your University account was recently changed.</p>
            <div style="background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0; color: #721c24; border-radius: 4px;">
                <strong>Security Alert:</strong> If you did not make this change, please immediately reset your password using the "Forgot Password" link and contact the IT department.
            </div>
            """;
        return getBaseTemplate("Security Alert: Password Changed", content);
    }
}