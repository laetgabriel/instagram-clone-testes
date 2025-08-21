"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import Image from "next/image";

export default function Feed() {
  const router = useRouter();
  const [isCheckingToken, setIsCheckingToken] = useState(true);

  // Verifica se o token existe
  useEffect(() => {
    const token = localStorage.getItem("jwtToken");
    if (!token) {
      router.push("/");
    } else {
      setIsCheckingToken(false);
    }
  }, [router]);

  const handleLogout = () => {
    // Remove o token do localStorage
    localStorage.removeItem("jwtToken");
    // Redireciona para a página de login
    router.push("/");
  };

  if (isCheckingToken) {
    // Enquanto verifica o token, mostra apenas um carregando (ou nada)
    return <div className="text-center mt-10">loading...</div>;
  }

  return (
    <div className="grid items-center justify-items-center min-h-screen p-8 pb-20 gap-16 sm:p-20 font-[family-name:var(--font-geist-sans)]">
      <main className="flex flex-col gap-8 items-center">
        <h1 className="text-3xl">Você está logado.</h1>
        <Image
          src={
            "https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExM3hhcHVrMXBjbGxhNmd6NGIxejB2Y3U3cmRhbjAxMXA5OTB6YzV0eCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/26FmRMFqoOcs18t9e/giphy.webp"
          }
          layout={"responsive"}
          height={175}
          width={175}
          alt={`friends`}
          unoptimized={true}
        />
        <h1 className="text-3xl">GOOD JOB! 👍</h1>
        <h1 className="text-1xl">
          Aqui será o feed, devs working. 👷⚙️
        </h1>
        {/* Botão de Logout */}
        <button
          onClick={handleLogout}
          className="mt-4 px-6 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition duration-300"
        >
          Logout
        </button>
      </main>
    </div>
  );
}
