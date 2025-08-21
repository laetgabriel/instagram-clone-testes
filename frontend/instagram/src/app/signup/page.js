"use client"

import { useState } from 'react';
import { createUser } from '../services/api/signup';
import Image from 'next/image';
import { useRouter } from 'next/navigation';
import Footer from '../components/footer';

export default function Signup() {

  const router = useRouter();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    fullName: '',
    username: '',
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const newUser = {
      fullName: formData.fullName,
      username: formData.username,
      email: formData.email,
      password: formData.password,
    };

    try {
      const data = await createUser(newUser);
      setSuccess('User created successfully!');
      setTimeout(() => {
                router.push('/');
            }, 2000);
    } catch (error) {
      setError(error.message);
    }
  };

  return (
    <div className="grid grid-rows-[20px_1fr_20px] items-center justify-items-center min-h-screen p-8 pb-20 gap-16 sm:p-20 font-[family-name:var(--font-geist-sans)]">
      <main className="h-full flex flex-row gap-8 row-start-2 items-center sm:items-start">
        <div className="flex flex-col border w-[100%] h-[100%] justify-items-center">
          <Image
            className="mx-auto"
            src={`/images/instagram-text-logo.png`}
            alt={"instagram text logo"}
            width="200"
            height="100"
          />
          <div className="w-[80%] mt-5 mx-auto text-center">
            Sign up to see photos and videos from your friends.
          </div>
          <div className="justify-items-center w-full grow">
            <form
              onSubmit={handleSubmit}
              className="mt-10 mb-10 w-64 flex flex-col gap-3 mx-auto"
            >
              <input
                className="w-full rounded border bg-gray-100 p-2 text-xs"
                name="email"
                type="text"
                placeholder="Entre com seu Email"
                value={formData.email}
                onChange={handleChange}
              />
              <input
                className="w-full rounded border bg-gray-100 p-2 text-xs"
                name="password"
                type="password"
                placeholder="Entre com sua senha"
                value={formData.password}
                onChange={handleChange}
              />
              <input
                className="w-full rounded border bg-gray-100 p-2 text-xs"
                name="fullName"
                type="text"
                placeholder="Entre com seu nome completo"
                value={formData.fullName}
                onChange={handleChange}
              />
              <input
                className="w-full rounded border bg-gray-100 p-2 text-xs"
                name="username"
                type="text"
                placeholder="Entre com seu username"
                value={formData.username}
                onChange={handleChange}
              />
              <button
                type="submit"
                className="duration-700 hover:bg-blue-800 w-full rounded border bg-[#0095F6] p-2 text-xs text-white font-bold"
              >
                Sign up
              </button>
            </form>
            {success && <div className="text-green-500 text-center">{success}</div>}
            {error && <div className="text-red-500 text-center">{error}</div>}
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
}

