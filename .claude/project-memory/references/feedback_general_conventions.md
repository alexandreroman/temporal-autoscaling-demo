---
name: "General conventions: language and line length"
description: "English only, line length limits for code and prose"
type: feedback
---

# General conventions: language and line length

- All code, comments, and text in English only.
- Max line length: 120 columns for code,
  80 columns for prose (comments, docs,
  CLAUDE.md, Markdown).

**Why:** consistent style across contributors
keeps diffs readable and avoids mixed-language
maintenance overhead.

**How to apply:** wrap prose at 80, code at 120.
Translate non-English content found in the
codebase opportunistically when touched.
