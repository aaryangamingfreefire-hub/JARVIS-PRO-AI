const http = require("http");
const API_KEY = process.env.GEMINI_API_KEY;
const MODEL = process.env.GEMINI_MODEL || "gemini-2.5-flash";
const PORT = 8787;

function send(res, code, obj) {
  res.writeHead(code, {"Content-Type":"application/json","Access-Control-Allow-Origin":"http://127.0.0.1"});
  res.end(JSON.stringify(obj));
}
const server = http.createServer(async (req,res) => {
  if (req.method === "GET" && req.url === "/health") return send(res,200,{ok:true,model:MODEL});
  if (req.method !== "POST" || req.url !== "/chat") return send(res,404,{error:"Not found"});
  if (!API_KEY) return send(res,500,{error:"Set GEMINI_API_KEY in Termux"});
  let raw="";
  req.on("data",d=>raw+=d);
  req.on("end",async()=>{
    try {
      const body=JSON.parse(raw);
      const prompt=String(body.message||"").slice(0,12000);
      const response=await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent?key=${API_KEY}`,{
        method:"POST",headers:{"Content-Type":"application/json"},
        body:JSON.stringify({system_instruction:{parts:[{text:"You are JARVIS PRO, a helpful female-style multilingual assistant. Reply naturally in the user's language. Never claim to have performed actions you did not perform. Ask for confirmation before sending messages, submitting job applications, or handling sensitive information. Do not invent CV qualifications."}]},contents:[{parts:[{text:prompt}]}]})
      });
      const data=await response.json();
      if(!response.ok) return send(res,response.status,{error:data.error?.message||"Gemini API error"});
      const reply=data.candidates?.[0]?.content?.parts?.map(p=>p.text||"").join("")||"No answer returned.";
      send(res,200,{reply});
    } catch(e){send(res,500,{error:String(e.message)});}
  });
});
server.listen(PORT,"127.0.0.1",()=>console.log(`JARVIS backend listening at http://127.0.0.1:${PORT}`));
