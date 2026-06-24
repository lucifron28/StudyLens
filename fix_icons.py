import re
with open("docs/architecture.d2", "r") as f: text = f.read()
new_text = re.sub(r"icon:\s*(mdi|si):([a-zA-Z0-9_-]+)", lambda m: f"icon: \"https://api.iconify.design/{m.group(1)}:{m.group(2)}.svg\"", text)
with open("docs/architecture.d2", "w") as f: f.write(new_text)

