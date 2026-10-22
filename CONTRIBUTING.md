# Contributing

Thanks for helping. This project grows one verified device and one language
at a time. Three kinds of contributions matter most:

## 1. Language packs (no coding)

Copy `launcher/lang/en.txt`, translate the **values** (keep the keys and the
`__name` / `__locale` lines accurate), and open a pull request adding your
file as `launcher/lang/<code>.txt`. Full guide: `docs/adding-a-language.md`.

## 2. Device reports (no coding)

Run `kit/0-PROBE.bat` against your projector/TV, then open an issue and
attach the generated `PROBE-RESULT.txt` and screenshot. That file is what a
support entry for your model gets built from. Guide:
`docs/new-device-support.md`.

## 3. Code and package lists

Rules of the house — they exist because a wrong line can blank someone's
home screen:

- **Never submit a package list you have not run yourself** on the exact
  device model and firmware you name. Mark entries `reported` until a second
  owner confirms, `tested` only after a reboot + a day of normal use.
- **Never-disable is sacred.** Packages that drive optics, focus, keystone,
  the gimbal, the remote, HDMI inputs or Bluetooth are listed per device in
  `data/<device>/never-disable.txt`. PRs that disable them will be closed.
- Disable in small batches and say so in the PR description: which batch,
  what you checked afterwards (focus? remote? HDMI switching? settings
  panel still opens?).
- No binaries in pull requests: no APKs, no `adb.exe`, no keystores. Release
  zips are assembled by the maintainer.
- The launcher is plain Java with no libraries and builds without Gradle.
  Keep it that way — it has to run well on 1–3 GB RAM projectors.

## Conduct

Be kind; many contributors here are owners, not programmers. Explain changes
in plain language, in the PR and in commit messages.
