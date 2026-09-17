# Security Policy

## Supported Versions and End-of-Life (EOL) Notice

Mirth Connect transitioned to a commercial, proprietary licensing model beginning with release **4.6.0**.

- **Active Commercial Support:** Security updates, vulnerability assessments, and maintenance patches are provided exclusively for active, licensed commercial versions of Mirth Connect (versions **4.6.0 and higher**). Patches and remediations are released within current commercial maintenance streams.

- **Open-Source End-of-Life (EOL):** All open-source versions of Mirth Connect (versions **4.5.2 and lower**) are officially **End-of-Life (EOL), unsupported, and retired**. NextGen Healthcare does not evaluate, backport, patch, or issue security advisories for open-source releases under any circumstances.

| Software Version | Release Type | Security Support Status |
|---|---|---|
| Mirth Connect 4.6.x and higher | Commercial (Licensed) | **Supported** (Security Updates in Latest Generally Released Commercial Version) |
| Mirth Connect 4.5.2 and lower | Open Source (Legacy) | **End-of-Life / Unsupported** (No Patches) |

*For organizations operating legacy open-source deployments, please contact [mirthconnectsales@nextgen.com](mailto:mirthconnectsales@nextgen.com) to migrate to a supported commercial release to maintain security, compliance, ongoing patch coverage, and professional support.*

## Reporting a Vulnerability

### For Licensed Commercial Customers and Security Researchers

If you discover a potential security vulnerability affecting **supported commercial versions (4.6.x or higher)**, please report it confidentially:

- **Email:** [mirthconnect-security@nextgen.com](mailto:mirthconnect-security@nextgen.com)

- **Information to Include:**
  - Specific commercial version(s) tested and verified.
  - Step-by-step instructions or proof-of-concept demonstrating reproduction.
  - Potential impact assessment and affected components.
  - Any relevant configuration details or environment prerequisites.

Our security engineering team will acknowledge receipt, conduct triage, and coordinate remediation according to our responsible disclosure policy.

### Important Policy Regarding Open-Source (<= 4.5.2) Reports

We recognize and appreciate the contributions of the independent security research community. However, please review our intake policy regarding retired open-source software:

1. **No Open-Source Remediation:** Vulnerability submissions evaluated exclusively against version 4.5.2 or earlier will not result in bug fixes, code changes, or public patches to the legacy open-source codebase.

2. **Commercial Cross-Evaluation:** Incoming disclosures are reviewed internally solely to determine whether the reported issue impacts active commercial releases (4.6.0+). If applicable, security fixes are developed and distributed exclusively within supported commercial releases.

3. **CVE Metadata & EOL Attribution:** For publicly coordinated CVE assignments with MITRE or authorized CNAs involving legacy open-source versions, NextGen Healthcare requests the inclusion of standard End-of-Life attribution:

   > *"NOTE: This vulnerability impacts a legacy open-source version of Mirth Connect (<= 4.5.2) that is officially End-of-Life and unsupported by NextGen Healthcare. Patches and security remediations are maintained exclusively in commercial releases 4.6.0 and higher."*
