"use client";

import { useState, useEffect } from "react";
import { useRouter } from 'next/navigation';
import { loginUser } from './services/api/signin';
import Image from "next/image";
import Link from "next/link";
import Footer from "./components/footer";

export default function Home() {

  const router = useRouter();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isCheckingToken, setIsCheckingToken] = useState(true);

  // Verifica se o token existe
  useEffect(() => {
    const token = localStorage.getItem("jwtToken");
    if (token) {
      router.push("/feed");
    } else {
      setIsCheckingToken(false);
    }
  }, [router]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    try {
      const data = await loginUser({ username, password });
      localStorage.setItem("jwtToken", data.token);
      router.push('/feed');

    } catch (error) {
      setError("Login failed: " + error.message);
    }

    setIsLoading(false);
  };

  if (isCheckingToken) {
    // Enquanto verifica o token, mostra apenas um carregando (ou nada)
    return <div className="text-center mt-10">loading...</div>;
  }

  return (
    <div className="grid grid-rows-[20px_1fr_20px] items-center justify-items-center min-h-screen p-8 pb-20 gap-16 sm:p-20 font-[family-name:var(--font-geist-sans)]">
      <main className="flex flex-row gap-8 row-start-2 items-center sm:items-start">
        <Image src={`/images/home-phones-2x.png`} priority={true} alt={"2 phones"} width="380" height="580" />

        <div>
          <main className="w-[350px] h-[500px] justify-items-center">
            <div className="flex flex-col border w-[100%] h-[80%] justify-items-center">
              <Image className="mx-auto" src={`/images/instagram-text-logo.png`} alt={"instagram text logo"} width="200" height="100" />
              <div className="justify-items-center w-full grow">
                <form onSubmit={handleSubmit} className="mt-10 w-64 flex flex-col gap-3 mx-auto">
                  <input
                    className="w-full rounded border bg-gray-100 p-2 text-xs"
                    name="username"
                    type="text"
                    placeholder="Entre com seu Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                  />
                  <input
                    className="w-full rounded border bg-gray-100 p-2 text-xs"
                    name="password"
                    type="password"
                    placeholder="Entre com sua senha"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                  <button
                    type="submit"
                    className="duration-700 hover:bg-blue-800 w-full rounded border bg-[#0095F6] p-2 text-xs text-white font-bold"
                    disabled={isLoading}
                  >
                    {isLoading ? "Carregando..." : "Log in"}
                  </button>
                </form>
                {error && <div className="text-red-500 mt-2">{error}</div>}
                <div className="flex space-x-2 w-64 mt-4 items-center">
                  <span className="bg-gray-300 h-px flex-1" />
                  <span className="p-2 uppercase text-xs text-gray-400 font-semibold">or</span>
                  <span className="bg-gray-300 h-px flex-1" />
                </div>
                <button className="mt-4 flex mx-auto">
                  <div className="bg-no-repeat facebook-logo mr-1"><Image src={`/images/facebook-logo.png`} alt={"facebook logo"} width="20" height="20" /></div>
                  <span>Log in with Facebook</span>
                </button>
                <button className="mt-4 flex mx-auto">
                  <span>Forgot Password</span>
                </button>
              </div>
            </div>

            <div className="flex mt-5 border w-[100%] h-[15%] items-center justify-items-center">
              <div className="mx-auto">Don't have an account? <Link className="hover:underline text-blue-600 hover:text-blue-800 visited:text-purple-600" href="/signup/">Sign up</Link></div>
            </div>
          </main>

        </div>
      </main>

      {<Footer />}
    </div>
  );
}
