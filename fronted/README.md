# fronted (Data Warehouse Frontend)

This is the frontend project for the Data Warehouse course. It uses Vue 3, Vite and Element Plus.

Quick start
1. Change to project folder:
   ```bash
   cd "E:\大三上\数据存储与管理\Data_Storage_and_Management\fronted"
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run development server:
   ```bash
   npm run dev
   ```
4. Open the address shown by Vite (e.g. `http://localhost:5173/`).

Notes about Git and line endings
- We intentionally do **not** track `node_modules` in the repo. If you previously committed `node_modules`, remove it from tracking (see commands below).
- To avoid LF/CRLF warnings on Windows, we include `.gitattributes` that normalizes text files to LF in the repository.

Commands to run locally (one-time cleanup if `node_modules` was committed)
```bash
# 1) Add .gitignore (already present) and remove tracked node_modules while keeping local files:
git rm -r --cached node_modules
git add .gitignore .gitattributes README.md
git commit -m "Remove node_modules from repository and add ignore/gitattributes"

# 2) Normalize line endings (if needed):
git add --renormalize .
git commit -m "Normalize line endings"

# 3) (Optional) recommended local setting on Windows:
git config --global core.autocrlf true
```

After this, collaborators can clone the repo and run `npm install` to restore dependencies.


