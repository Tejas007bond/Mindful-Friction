Here is the full code for the README.md file so you can copy and paste it directly.

Markdown
# Mindful Friction

Mindful Friction is a browser extension and productivity tool designed to reduce compulsive habits and mindless web browsing. Instead of abruptly blocking access to websites, the application introduces intentional, gentle friction (such as short pause timers, reflection prompts, or intentional interaction requirements) to encourage conscious digital habits and mindful web navigation.

---

## Features

* **Intentional Delays**: Introduces customizable pauses before opening distracting or targeted websites.
* **Reflection Prompts**: Asks users to confirm their intent or state a quick reason before granting access to specific URLs.
* **Custom Rules & Site Lists**: Configure tailored rules, target sites, and custom friction levels depending on personal productivity requirements.
* **Minimalist Interface**: Clean, accessible, and lightweight design that integrates seamlessly into the browser workflow.
* **Privacy Focused**: Operates locally within the browser without tracking, logging, or sending browsing activity to external servers.

---

## Project Structure

Mindful-Friction/
├── assets/          # Static assets (icons, images, styles)
├── src/             # Core application code
│   ├── background/  # Background scripts and event handling
│   ├── content/     # Content scripts injected into targeted pages
│   ├── popup/       # Interface logic and view files
│   └── options/     # Settings and configuration management
├── manifest.json    # Extension configuration file
├── package.json     # Node.js dependencies and build configurations
└── README.md        # Documentation


---

## Installation and Local Setup

### Prerequisites

* Node.js (version 16.x or higher)
* npm, yarn, or pnpm
* Google Chrome, Brave, Edge, or any Chromium-based browser

### Clone the Repository

```bash
git clone [https://github.com/Tejas007bond/Mindful-Friction.git](https://github.com/Tejas007bond/Mindful-Friction.git)
cd Mindful-Friction
Install Dependencies
Bash
npm install
Build the Project
Run the build command to generate the production extension bundle:

Bash
npm run build
Loading the Extension into Browser
Open your Chromium-based web browser (e.g., Google Chrome).

Navigate to chrome://extensions/ in the address bar.

Enable Developer mode using the toggle switch in the top-right corner.

Click on the Load unpacked button in the top-left area.

Select the build output directory (dist or the root project folder containing manifest.json).

The Mindful Friction extension will now appear in your browser toolbar.

Usage Guide
Click on the Mindful Friction extension icon in your browser toolbar.

Open the Settings / Options panel to manage site configurations.

Add domain names or patterns for websites you frequently visit mindlessly (e.g., social media platforms, entertainment sites).

Select the friction type:

Pause Timer: Wait a specified number of seconds before page entry.

Intention Prompt: Type a short reason for visiting the site before proceeding.

Save your preferences. Whenever a restricted site is opened, the configured friction screen will intercept access.

Development
To start local development with hot-reloading enabled:

Bash
npm run dev
Running Tests
Bash
npm run test
Contributing
Contributions are welcome. Follow these steps to submit changes:

Fork the repository.

Create a new branch (git checkout -b feature/your-feature-name).

Commit your changes (git commit -m "Add new feature").

Push to the branch (git push origin feature/your-feature-name).

Open a Pull Request with a clear description of your modifications.

License
This project is licensed under the MIT License. See the LICENSE file for full details.
