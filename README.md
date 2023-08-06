#Router

**A simple Android routing framework, which references Alibaba's ARouter framework.**

**Specific usage:**
  1. Add the @EnableRouter annotation to the Android entry class Application.
  2. Add the @RequestMapping annotation to the Activity class with the URL and description.
  3. When using, call this method: Router.getInstance().redirect("/xxx", new Bundle());.
